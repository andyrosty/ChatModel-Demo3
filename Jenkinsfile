pipeline {
    agent any
    
    environment {
        AWS_CREDENTIALS = credentials('aws-credentials')
        TERRAFORM_DIR = 'terraform'
        ECR_REPO = credentials('ecr-repository-url')
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
                sh '''
                  echo "Starting Maven Build"
                  mvn clean package
                  echo "Build finished"
                '''
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
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

    }
    
}