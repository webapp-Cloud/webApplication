#!/bin/bash
set -e

echo "Starting application setup..."

# Check if user exists first
if ! id csye6225 > /dev/null 2>&1; then
    echo "Error: Required user 'csye6225' does not exist"
    echo "Please run setup_system.sh first"
    exit 1
fi

# Check if group exists
if ! getent group csye6225 > /dev/null 2>&1; then
    echo "Error: Required group 'csye6225' does not exist"
    echo "Please run setup_system.sh first"
    exit 1
fi

# Check if required directories exist
for dir in "/opt/csye6225" "/etc/csye6225" "/var/log/webapp"; do
    if [ ! -d "$dir" ]; then
        echo "Error: Required directory $dir does not exist"
        echo "Please run setup_system.sh first"
        exit 1
    fi
done
##!/bin/bash
#set -e
#
echo "Starting application setup..."

# Define variables
APP_USER="csye6225"
APP_GROUP="csye6225"
APP_DIR="/opt/csye6225"
CONFIG_DIR="/etc/csye6225"

# Check and move JAR file
if [ ! -f /tmp/webapp.jar ]; then
    echo "Error: webapp.jar not found in /tmp"
    exit 1
fi

# Move JAR file to application directory
echo "Moving application files..."
mv /tmp/webapp.jar ${APP_DIR}/webapp.jar

# Set up systemd service
echo "Setting up systemd service..."
if [ -f /tmp/csye6225.service ]; then
    mv /tmp/csye6225.service /etc/systemd/system/csye6225.service
else
    echo "Error: Service file not found"
    exit 1
fi

# Create environment file
echo "Creating environment file..."
cat > ${CONFIG_DIR}/application-env << EOL
# Default environment variables - will be replaced by user data
DB_HOST=localhost
DB_PORT=5432
DB_NAME=csye6225
DB_USER=csye6225
DB_PASSWORD=placeholder
SPRING_PROFILES_ACTIVE=dev
EOL

# Set correct ownership and permissions
echo "Setting permissions..."
chown ${APP_USER}:${APP_GROUP} ${APP_DIR}/webapp.jar
chmod 500 ${APP_DIR}/webapp.jar
chown ${APP_USER}:${APP_GROUP} ${CONFIG_DIR}/application-env
chmod 600 ${CONFIG_DIR}/application-env
chown root:root /etc/systemd/system/csye6225.service
chmod 644 /etc/systemd/system/csye6225.service

# Enable and start service
echo "Configuring service..."
systemctl daemon-reload
systemctl enable csye6225.service
# Don't start the service now - it will start with proper configuration from user-data

echo "Application setup completed successfully"
ls -la ${APP_DIR}
ls -la ${CONFIG_DIR}