# Cloud Infrastructure Setup

This repository contains Infrastructure as Code (IaC) for setting up a secure cloud environment using Terraform. The infrastructure includes EC2 instances, RDS databases, S3 storage, and various AWS services configured with security best practices.

## Prerequisites

- AWS CLI configured with appropriate credentials
- Terraform v1.9.0 or higher
- Java Development Kit (JDK) 21
- Maven
- SendGrid account for email services
- Domain name registered in Route 53
- SSL certificate (for demo environment)

## Infrastructure Components

### Key Management Service (KMS)

Separate encryption keys are created for:
- EC2 instances
- RDS database
- S3 buckets
- Secrets Manager (for database passwords and email credentials)

All KMS keys are configured with:
- 90-day rotation period
- 7-day deletion window
- Region-specific deployment

### Database Configuration

The RDS instance is configured with:
- PostgreSQL 15.8
- Custom parameter group
- Encrypted storage using KMS
- Automated password management through Secrets Manager
- Private subnet placement
- Security group restrictions

### Application Security

- SSL/TLS configuration:
    - Dev: AWS Certificate Manager
    - Demo: Imported certificate from third-party vendor

To import an SSL certificate (Demo environment):
```bash
aws acm import-certificate \ 
  --certificate file://demo_oncloudwithrishabh_me.crt \
  --private-key file://privatekey.key\
  --certificate-chain file://demo_oncloudwithrishabh_me.ca-bundle \
  --region us-east-1
```

### Load Balancer Configuration

- Application Load Balancer (ALB) in public subnets
- HTTP/HTTPS support
- Health checks on '/healthz' endpoint
- No HTTP to HTTPS redirection required
- Internal communication uses HTTP

### Auto Scaling Configuration

- Minimum: 3 instances
- Maximum: 5 instances
- Desired: 3 instances
- CPU utilization-based scaling
- Health check grace period: 300 seconds
- Cooldown period: 60 seconds

### Network Architecture

- VPC with public and private subnets
- Internet Gateway for public access
- Route tables for traffic management
- Security groups for instance protection

### Application Components

1. Web Application:
    - Java Spring Boot application
    - Custom systemd service
    - CloudWatch logging integration
    - S3 integration for file storage

2. Lambda Function:
    - Email verification handler
    - SNS integration
    - SendGrid email service integration

## Deployment Instructions

1. Initialize Terraform:
```bash
terraform init
```

2. Review planned changes:
```bash
terraform plan
```

3. Apply infrastructure:
```bash
terraform apply
```

## Security Groups

1. Application Load Balancer:
    - Inbound: 80, 443 from anywhere
    - Outbound: All traffic

2. EC2 Instances:
    - Inbound: 8080 from ALB
    - Inbound: 22 from ALB (for maintenance)
    - Outbound: All traffic

3. RDS Database:
    - Inbound: 5432 from EC2 security group
    - Inbound: 5432 from Lambda security group
    - Outbound: All traffic

## Monitoring and Logging

- CloudWatch agent configured for application logs
- Custom metrics namespace: CSYE6225/Custom
- Log retention: 7 days
- Application logs path: /var/log/webapp/application.log

## Environment Variables

Required environment variables for the application:
- DB_HOST
- DB_PORT
- DB_NAME
- DB_USERNAME
- DB_PASSWORD
- AWS_REGION
- AWS_BUCKET_NAME
- AWS_SNS_TOPIC_ARN
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD

## Cleanup

To destroy the infrastructure:
```bash
terraform destroy
```

## Important Notes

1. Direct EC2 instance access is not allowed; all traffic must go through the load balancer
2. Database credentials are managed through AWS Secrets Manager
3. Application artifacts should be built before infrastructure deployment
4. SSL certificates must be imported before deploying in demo environment
5. All resources are tagged appropriately for cost tracking

## Troubleshooting

1. If SSL import fails, verify certificate chain format
2. For database connection issues, check security group rules
3. If auto-scaling isn't working, verify CloudWatch metrics
4. For email verification issues, check SNS and Lambda logs