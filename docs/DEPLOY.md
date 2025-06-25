# Deployment Guide – ChatModel Demo on AWS (us-east-1)

This document explains **all steps required to deploy the ChatModel Demo application to AWS**.  It covers first-time infrastructure provisioning with Terraform, continuous delivery with Jenkins, and manual fall-back commands.

---

## 1. High-level architecture

* **Amazon ECR** – container registry for the application image.
* **Amazon ECS Fargate** – serverless compute that runs the container.
* **Application Load Balancer** – public HTTPS entry-point.
* **Amazon VPC** – two public and two private subnets plus a NAT gateway.
* **AWS SSM Parameter Store** – keeps the `OPENAI_API_KEY` secret.
* **S3 + DynamoDB** – remote state + locking for Terraform.

Everything is described in `terraform/`. Jenkins builds, pushes, and applies the configuration on every successful build of the **main** branch.

---

## 2. Prerequisites

1. **AWS account** with permissions to create the resources above in **us-east-1**.
2. **Local tooling** (only needed for manual Terraform runs):
   - Terraform >= 1.0
   - AWS CLI >= 2.0 (configured)
   - Docker, Maven 3.9 & JDK 17 for local builds/tests
3. **Jenkins LTS** with Docker socket access _(controller is allowed to build images)_.
4. Jenkins plugins:
   - Git, Pipeline, BlueOcean
   - Docker Pipeline
   - AWS Credentials & AWS Steps
   - SonarQube Scanner (anonymous)
   - Lockable Resources

---

## 3. One-time AWS preparation

### 3.1 Create an ECR repository

```bash
aws ecr create-repository \
  --repository-name chatmodel-demo \
  --image-scanning-configuration scanOnPush=false \
  --region us-east-1
```

Copy the URI (`<account>.dkr.ecr.us-east-1.amazonaws.com/chatmodel-demo`). This will be stored in Jenkins as a **Secret Text** credential with the ID `ecr-repository-url`.

### 3.2 Remote Terraform state

```bash
# S3 bucket for the state
aws s3 mb s3://chatmodel-tf-state-us-east-1

# DynamoDB table for state locking
aws dynamodb create-table \
  --table-name chatmodel-tf-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

Create a local file `terraform/backend.tf` (do **not** commit if you prefer) containing:

```hcl
terraform {
  backend "s3" {
    bucket         = "chatmodel-tf-state-us-east-1"
    key            = "chatmodel-demo/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "chatmodel-tf-locks"
    encrypt        = true
  }
}
```

### 3.3 Store secrets

```bash
aws ssm put-parameter \ 
  --name "/chatmodel/openai-api-key" \ 
  --type "SecureString" \ 
  --value "<YOUR-OPENAI-KEY>" \ 
  --region us-east-1
```

---

## 4. Jenkins configuration

### 4.1 Credentials

| ID                    | Type                 | Note                                                                  |
|-----------------------|----------------------|-----------------------------------------------------------------------|
| `aws-credentials`     | AWS access key/secret| Allows pipeline to call AWS CLI & Docker login to ECR                 |
| `ecr-repository-url`  | Secret text          | The URI from step 3.1                                                 |

### 4.2 Global tools / plugins

* JDK 17 (tool name `temurin-17` – matches `Jenkinsfile`)
* Docker Pipeline, AWS plugins, SonarQube Scanner, Lockable Resources

### 4.3 Lockable resource

Add a resource named **`terraform-chatmodel`** – no labels, single instance. The pipeline locks it around `terraform apply`.

### 4.4 SonarQube server (optional)

Under *Manage Jenkins → Configure System → SonarQube Servers* create a server named `SonarQube`.  Leave the authentication token blank (anonymous access).

---

## 5. First-time deployment (CI/CD path)

1. Push the repository to your Git host and create a new multibranch pipeline in Jenkins pointing at it. Jenkins will detect the `Jenkinsfile`.
2. Run the job on **any** branch – this will build the Docker image and push it to ECR (tagged with the Jenkins build number) and run `terraform plan`.
3. Merge to **`main`**.  The pipeline repeats the build and then performs `terraform apply`, provisioning all infrastructure and deploying the freshly built image.
4. After the build is **green**, browse to *EC2 → Load Balancers*.  The DNS name of `chatmodel-alb` is your public HTTPS endpoint.

---

## 6. Manual Terraform workflow (optional)

```bash
cd terraform

# Initialise (only once, or when the backend or providers change)
terraform init

# Plan – image_tag must reference an image that already exists in ECR
terraform plan -var="image_tag=<build-number>" -out tfplan

# Apply
terraform apply -auto-approve tfplan
```

---

## 7. Roll-back strategy

1. Identify the previous good container tag (Jenkins build number).
2. Re-run the Jenkins job with `IMAGE_TAG` parameter set to that number **or** trigger a commit that updates `image_tag` for Terraform.
3. Jenkins executes `terraform apply`, replacing the running tasks. Because ECS Fargate is behind an ALB, the update is rollout-based and results in zero downtime.

---

## 8. FAQ

**Q – Where do I tweak CPU / memory?**  → Edit `cpu` / `memory` in `aws_ecs_task_definition.app_task`.

**Q – How many tasks run?**  → `desired_count` in `aws_ecs_service.app_service` (Terraform).

**Q – I need a staging environment.**  → Create a second Terraform workspace (or duplicate the state bucket/prefix) and duplicate the Jenkins job with a different parameters set.

---

Happy shipping! 🚀
