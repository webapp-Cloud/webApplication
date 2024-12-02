#!/bin/bash
set -e

echo "Starting system setup..."

# Update and upgrade the system
apt-get update
apt-get upgrade -y

# Install required packages (no PostgreSQL, only Java)
apt-get install -y openjdk-21-jdk maven curl unzip


apt-get install -y \
    openjdk-21-jdk \
    postgresql-client \
    unzip \
    jq \
    python3-pip

# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip


# Install CloudWatch Agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
dpkg -i -E ./amazon-cloudwatch-agent.deb
rm -f ./amazon-cloudwatch-agent.deb

# Create application user and group
useradd -m -s /bin/bash csye6225
groupadd -f csye6225
usermod -aG csye6225 csye6225

# Create necessary directories
mkdir -p /opt/csye6225
mkdir -p /etc/csye6225
mkdir -p /var/log/webapp

# Set directory permissions
chown -R csye6225:csye6225 /opt/csye6225
chown -R csye6225:csye6225 /etc/csye6225
chown -R csye6225:csye6225 /var/log/webapp

chmod 755 /opt/csye6225
chmod 755 /etc/csye6225
chmod 755 /var/log/webapp
touch /var/log/webapp/application.log
chown csye6225:csye6225 /var/log/webapp/application.log
chmod 644 /var/log/webapp/application.log

# Clean up
apt-get clean
rm -rf /var/lib/apt/lists/*

echo "System setup completed"
