#!/bin/bash
set -e

# Update and upgrade the system
sudo apt-get update
sudo apt-get upgrade -y

# Install necessary packages
sudo apt-get install -y openjdk-17-jdk tomcat9 mysql-server

# Start and enable Tomcat service
sudo systemctl start tomcat9
sudo systemctl enable tomcat9

# Start and enable MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Optionally, secure MySQL installation
# sudo mysql_secure_installation

# Install any additional dependencies your application might need
# For example, if you need Maven:
# sudo apt-get install -y maven

# Clone your application repository (replace with your actual repository URL)
# git clone https://github.com/your-username/your-repo.git

# Build your application (if necessary)
# cd your-repo
# mvn clean install

# Deploy your application to Tomcat
# sudo cp target/your-app.war /var/lib/tomcat9/webapps/

# Restart Tomcat to apply changes
sudo systemctl restart tomcat9

echo "Setup completed successfully!"