# Deploying Your Spring Boot Application with GitHub, Jenkins, Terraform, and AWS

Based on your project structure, I can see you have a Spring Boot application that interacts with OpenAI. Let me walk you through a comprehensive deployment process using GitHub, Jenkins, Terraform, and AWS with security best practices.

## 1. Overall Deployment Pipeline

Here's the high-level workflow we'll implement:
1. Code stored in GitHub repository with branch protection
2. Jenkins pipeline triggered on code changes
3. Terraform manages AWS infrastructure
4. Application deployed to AWS services

## 2. GitHub Setup

### Repository Configuration
- Create a repository for your application code
- Create a separate repository for your infrastructure code (Terraform)
- Implement branch protection rules:
  - Require pull request reviews before merging
  - Require status checks to pass before merging
  - Require signed commits

### Branching Strategy
- Implement GitFlow or trunk-based development
- Main/master branch for production code
- Development branch for ongoing work
- Feature branches for new features

### Security Practices
- Use `.gitignore` to prevent committing sensitive files
- Store secrets in GitHub Secrets, not in code
- Implement pre-commit hooks to scan for secrets
- Regular dependency scanning with Dependabot

## 3. Jenkins Setup

### Jenkins Configuration
- Set up a Jenkins server (preferably in AWS)
- Secure Jenkins with proper authentication
- Install necessary plugins:
  - Git integration
  - AWS plugins
  - Terraform plugin
  - Pipeline
  - SonarQube (for code quality)

### Pipeline Configuration
Create a `Jenkinsfile` in your repository:

```groovy
pipeline {
    agent any
    
    environment {
        AWS_CREDENTIALS = credentials('aws-credentials')
        TERRAFORM_DIR = 'terraform'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t chatmodel-demo:${BUILD_NUMBER} .'
            }
        }
        
        stage('Push to ECR') {
            steps {
                sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REPO}'
                sh 'docker tag chatmodel-demo:${BUILD_NUMBER} ${ECR_REPO}/chatmodel-demo:${BUILD_NUMBER}'
                sh 'docker push ${ECR_REPO}/chatmodel-demo:${BUILD_NUMBER}'
            }
        }
        
        stage('Terraform Init') {
            steps {
                dir(TERRAFORM_DIR) {
                    sh 'terraform init'
                }
            }
        }
        
        stage('Terraform Plan') {
            steps {
                dir(TERRAFORM_DIR) {
                    sh 'terraform plan -var="image_tag=${BUILD_NUMBER}" -out=tfplan'
                }
            }
        }
        
        stage('Terraform Apply') {
            when {
                branch 'main'
            }
            steps {
                dir(TERRAFORM_DIR) {
                    sh 'terraform apply -auto-approve tfplan'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```

## 4. Terraform Infrastructure

Create a Terraform directory with the following files:

### `main.tf`
```hcl
provider "aws" {
  region = var.aws_region
}

# VPC and networking
module "vpc" {
  source = "terraform-aws-modules/vpc/aws"
  
  name = "chatmodel-vpc"
  cidr = "10.0.0.0/16"
  
  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
  
  enable_nat_gateway = true
  single_nat_gateway = true
  
  tags = var.common_tags
}

# Security groups
resource "aws_security_group" "app_sg" {
  name        = "chatmodel-app-sg"
  description = "Security group for the application"
  vpc_id      = module.vpc.vpc_id
  
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = var.common_tags
}

# ECS Cluster
resource "aws_ecs_cluster" "app_cluster" {
  name = "chatmodel-cluster"
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  
  tags = var.common_tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "app_task" {
  family                   = "chatmodel-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn
  
  container_definitions = jsonencode([
    {
      name      = "chatmodel-container"
      image     = "${var.ecr_repository_url}:${var.image_tag}"
      essential = true
      
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "production"
        }
      ]
      
      secrets = [
        {
          name      = "OPENAI_API_KEY"
          valueFrom = aws_ssm_parameter.openai_api_key.arn
        }
      ]
      
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/chatmodel"
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
  
  tags = var.common_tags
}

# ECS Service
resource "aws_ecs_service" "app_service" {
  name            = "chatmodel-service"
  cluster         = aws_ecs_cluster.app_cluster.id
  task_definition = aws_ecs_task_definition.app_task.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  
  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = false
  }
  
  load_balancer {
    target_group_arn = aws_lb_target_group.app_tg.arn
    container_name   = "chatmodel-container"
    container_port   = 8080
  }
  
  deployment_controller {
    type = "ECS"
  }
  
  tags = var.common_tags
}

# Application Load Balancer
resource "aws_lb" "app_lb" {
  name               = "chatmodel-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = module.vpc.public_subnets
  
  tags = var.common_tags
}

resource "aws_lb_target_group" "app_tg" {
  name        = "chatmodel-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"
  
  health_check {
    path                = "/actuator/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 3
    unhealthy_threshold = 3
  }
  
  tags = var.common_tags
}

resource "aws_lb_listener" "app_listener" {
  load_balancer_arn = aws_lb.app_lb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = var.certificate_arn
  
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_tg.arn
  }
}

# IAM Roles
resource "aws_iam_role" "ecs_execution_role" {
  name = "chatmodel-ecs-execution-role"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
  
  tags = var.common_tags
}

resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "ecs_task_role" {
  name = "chatmodel-ecs-task-role"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
  
  tags = var.common_tags
}

# Parameter Store for secrets
resource "aws_ssm_parameter" "openai_api_key" {
  name        = "/chatmodel/openai-api-key"
  description = "OpenAI API Key"
  type        = "SecureString"
  value       = var.openai_api_key
  
  tags = var.common_tags
}
```

### `variables.tf`
```hcl
variable "aws_region" {
  description = "AWS region to deploy resources"
  default     = "us-east-1"
}

variable "ecr_repository_url" {
  description = "URL of the ECR repository"
}

variable "image_tag" {
  description = "Docker image tag to deploy"
}

variable "openai_api_key" {
  description = "OpenAI API Key"
  sensitive   = true
}

variable "certificate_arn" {
  description = "ARN of the SSL certificate for HTTPS"
}

variable "common_tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    Project     = "ChatModel"
    Environment = "Production"
    ManagedBy   = "Terraform"
  }
}
```

### `outputs.tf`
```hcl
output "alb_dns_name" {
  description = "DNS name of the load balancer"
  value       = aws_lb.app_lb.dns_name
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.app_cluster.name
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.app_service.name
}
```

## 5. Docker Configuration

Create a `Dockerfile` in your project root:

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 6. Security Best Practices

### Infrastructure Security
- Use private subnets for application components
- Implement least privilege IAM roles
- Enable AWS CloudTrail for auditing
- Use AWS Config for compliance monitoring
- Implement AWS WAF in front of your ALB
- Enable VPC Flow Logs

### Application Security
- Store secrets in AWS Parameter Store or Secrets Manager
- Implement HTTPS only
- Set up proper security headers
- Implement rate limiting
- Regular security scanning with tools like OWASP ZAP
- Implement AWS Shield for DDoS protection

### CI/CD Security
- Scan Docker images for vulnerabilities
- Implement infrastructure as code security scanning
- Use Jenkins credentials store for secrets
- Implement approval gates for production deployments
- Regular security audits of the pipeline

## 7. Monitoring and Logging
- Set up CloudWatch for monitoring
- Implement centralized logging with CloudWatch Logs
- Set up alarms for critical metrics
- Implement distributed tracing with AWS X-Ray
- Create a dashboard for application health