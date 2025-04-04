pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Build the dotNet app') {
            steps {
                echo 'Building...'
                bat 'dotnet build' //Commands for the build process
            }
        }

        stage('Build a Docker image') {
            steps {
                echo 'Building Docker image...'
                bat 'docker build -t basicdotnetapi .' //"basicdotnetapi" is the name of my app
            }
        }

        stage('Authenticate with AWS ECR'){
            steps{
                echo 'Authenticating with AWS ECR...'
                //apparently there is another way to get credentials from the jenkings agent (I dont know how to yet)
                //This next line reads SECRET and ACCESS Keys from my local .aws/credentials
                bat 'aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 177555066587.dkr.ecr.us-west-2.amazonaws.com'
            }
        }

        stage('Tag the image') {
            steps {
                echo 'Tagging the image...'
                //Tha tag given to this image is "latest" but it could be anything like 1.0 etc..
                bat 'docker tag basicdotnetapi:latest 177555066587.dkr.ecr.us-west-2.amazonaws.com/vallardo-dotnetapp:latest'
            }
        }

        stage('Push the image to Amazon Elastic Container Registry (ECR)') {
            steps {
                echo 'Pushing the image to Amazon Elastic Container Registry (ECR)...'
                bat 'docker push 177555066587.dkr.ecr.us-west-2.amazonaws.com/vallardo-dotnetapp:latest'
            }
        }
        
        stage('Connect to EC2 via SSH'){
            steps{
                echo 'Connecting to EC2 via ssh...'
                //where do ec2.pem is stored typically? in this case in in my local box in a random location
                //DO I NEED THE .PEM FILE AT ALL? HOW CAN I USE THE ROLE?
                bat 'ssh -i "../../vallardo_ec2.pem" ec2-user@ec2-44-245-155-98.us-west-2.compute.amazonaws.com'
            }
        }

        stage('Install AWS CLI on EC2 instance'){ //this stage is needed for ubuntu EC2 instancebut but not for Amazon Linux EC2 instances
            steps{
                echo 'Installing AWS CLI on the EC2 instance ...'
                bat 'sudo apt-get update -y'
                bat 'sudo apt-get install unzip curl -y'
                bat 'curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"'
                bat 'unzip awscliv2.zip'
                bat 'sudo ./aws/install'
                bat 'aws --version'
            }
        }

        stage('Install Docker on the EC2 instance'){
            steps{
                echo 'Installing Docker in EC2...'
                bat 'sudo apt-get update'
                bat 'sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common'
                bat 'curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -'
                bat 'sudo DEBIAN_FRONTEND=noninteractive add-apt-repository -y "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"'
                bat 'sudo apt-get update'
                bat 'sudo apt-get install -y docker-ce'
                bat 'sudo docker --version'
            }
        }

        stage('Pull the Docker image from ECR'){
            steps{
                echo 'Pulling docker image from ECR ...'
                //This steps assumes that the EC2 instance has a role assigned with permissions to log in to AWS ECR
                bat 'aws ecr get-login-password --region us-west-2 | sudo docker login --username AWS --password-stdin 177555066587.dkr.ecr.us-west-2.amazonaws.com'
                bat 'sudo docker pull 177555066587.dkr.ecr.us-west-2.amazonaws.com/vallardo-dotnetapp:latest'
            }
        }

        stage('Run the Docker container'){
            steps{
                echo 'Running the Docker container...'
                bat 'sudo docker run -d -p 80:8080 177555066587.dkr.ecr.us-west-2.amazonaws.com/vallardo-dotnetapp:latest'
            }
        }
    }
}
