<!--
  Jenkins Deployment Guide for ChatModel Demo
  ---------------------------------------------
  This document outlines multiple ways to deploy a Jenkins server
  for building, testing, and deploying the ChatModel Demo application
  on AWS.
-->
# Jenkins Server Deployment Guide

This guide describes three primary ways to stand up a Jenkins CI server
that integrates with AWS for ECR, ECS/Fargate, SSM, Terraform state,
and more:

1. Docker container (fastest proof-of-concept)
2. EC2 VM (Ubuntu) installation
3. Kubernetes (Helm chart)

---

## 1. Docker-based Jenkins (fastest!)

### 1.1 Run Jenkins container
```bash
# Create a volume for Jenkins data
docker volume create jenkins_home

# Run the official LTS image with Docker socket access
docker run -d \
  --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

### 1.2 Initial admin setup
- Wait ~30 s for Jenkins to initialize.
- Retrieve the initial admin password:
  ```bash
  docker exec jenkins \
    cat /var/jenkins_home/secrets/initialAdminPassword
  ```
- Browse http://localhost:8080, enter the password, install **Recommended Plugins**
  (Git, Pipeline, Docker Pipeline, AWS Steps, Lockable Resources, etc.),
  and create your first admin user.

### 1.3 Post-install configuration
- **Global Tools** (Manage Jenkins → Global Tool Configuration):
  - **JDK 17** (Tool name: `temurin-17`)
  - **Docker** (optional, if you want Jenkins to manage Docker installs)
- **Credentials** (Manage Jenkins → Credentials):
  - **AWS credentials** (ID: `aws-credentials`, type: AWS access key/secret)
  - **ECR repository URL** (ID: `ecr-repository-url`, type: Secret text)
- **Lockable Resources** (Manage Jenkins → Lockable Resources):
  - Resource name: `terraform-chatmodel` (1 instance)
- **SonarQube Servers** (Manage Jenkins → Configure System → SonarQube):
  - Server name: `SonarQube` (leave token blank for anonymous)

After this, your Jenkinsfile can simply reference these by name/ID.

---

## 2. EC2 VM-based Jenkins (Ubuntu)

### 2.1 Install Jenkins
```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# Add Jenkins Debian repository and key
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo \ 
  "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
   https://pkg.jenkins.io/debian-stable binary/" \
  | sudo tee /etc/apt/sources.list.d/jenkins.list
sudo apt-get update
sudo apt-get install -y jenkins
sudo systemctl enable --now jenkins
```

### 2.2 Access & configure
- Check status/logs:
  ```bash
  sudo systemctl status jenkins
  sudo journalctl -u jenkins -f
  ```
- Retrieve the initial admin password:
  ```bash
  sudo cat /var/lib/jenkins/secrets/initialAdminPassword
  ```
- Browse http://<your-server-ip>:8080, enter the password, follow the same
  plugin and post-install configuration as in Section 1.3.

---

## 3. Kubernetes-based Jenkins (Helm chart)

If you run Kubernetes (EKS or any k8s), deploy Jenkins via the official chart:
```bash
helm repo add jenkins https://charts.jenkins.io
helm repo update
helm install jenkins jenkins/jenkins \
  --set controller.adminUser=admin \
  --set controller.adminPassword=<strongpassword> \
  --set persistence.storageClass=standard

# Port-forward for local access
kubectl port-forward svc/jenkins 8080:8080
```
- Browse http://localhost:8080 and login with the above credentials.
- Configure IAM Roles for Service Accounts (IRSA) or mount AWS keys as secrets
  for AWS connectivity, then complete post-install steps from Section 1.3.

---

## 4. Common Post-install Steps

- Configure **Global Tools**, **Credentials**, **Lockable Resources**, and
  **SonarQube Servers** exactly as outlined in Section 1.3.
- If on AWS, prefer using **IAM instance profiles** (EC2) or **IRSA** (EKS)
  instead of long-lived access keys.
- Secure Jenkins behind a load balancer or use security groups to restrict
  access to trusted IPs/VPN only.

---

## 5. AWS-native Alternatives

- **AWS CodeBuild & CodePipeline**: Fully managed CI/CD; integrates out of the
  box with ECR, ECS, SSM, Terraform (via CodeBuild). No self-hosting needed.
- **AWS Amplify**: For simple web app deployments (mostly front-end).
  
---

*Happy building and deploying!* 🚀