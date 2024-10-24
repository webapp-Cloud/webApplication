#!/bin/bash
set -e

echo "Starting system setup..."

# Update and upgrade the system
apt-get update
apt-get upgrade -y

# Install required packages (no PostgreSQL, only Java)
apt-get install -y openjdk-17-jdk maven curl unzip

# Create application user and group
useradd -m -s /bin/bash csye6225
groupadd -f csye6225
usermod -aG csye6225 csye6225

# Create necessary directories
mkdir -p /opt/csye6225
mkdir -p /etc/csye6225

# Set directory permissions
chown -R csye6225:csye6225 /opt/csye6225
chown -R csye6225:csye6225 /etc/csye6225
chmod 755 /opt/csye6225
chmod 755 /etc/csye6225

# Clean up
apt-get clean
rm -rf /var/lib/apt/lists/*

echo "System setup completed"