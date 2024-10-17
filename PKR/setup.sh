#!/bin/bash

set -e

# Function for logging
log() {
    echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2
}

# Check if environment variables are set
if [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    log "Error: DB_URL, DB_USERNAME, or DB_PASSWORD is not set"
    exit 1
fi

# Extract database name from DB_URL
DB_NAME=$(echo $DB_URL | awk -F'/' '{print $NF}')

log "Updating system and installing dependencies"
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y openjdk-17-jdk wget gnupg2

log "Installing PostgreSQL"
# Add PostgreSQL repository
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo apt-get update

# Install PostgreSQL
sudo apt-get install -y postgresql-15 postgresql-contrib

log "Verifying PostgreSQL installation"
if ! command -v psql &> /dev/null; then
    log "Error: PostgreSQL installation failed"
    exit 1
fi

log "Setting up application user and directories"
sudo useradd -m -s /bin/bash csye6225 || log "User csye6225 already exists"
sudo groupadd -f csye6225
sudo usermod -aG csye6225 csye6225

sudo mkdir -p /opt/csye6225
sudo chown csye6225:csye6225 /opt/csye6225
sudo -u csye6225 mkdir -p /opt/csye6225/app
sudo mkdir -p /etc/csye6225
sudo chown csye6225:csye6225 /etc/csye6225

log "Setting up environment file"
echo "DB_URL=${DB_URL}" | sudo tee /etc/csye6225/application-env
echo "DB_USERNAME=${DB_USERNAME}" | sudo tee -a /etc/csye6225/application-env
echo "DB_PASSWORD=${DB_PASSWORD}" | sudo tee -a /etc/csye6225/application-env
sudo chmod 600 /etc/csye6225/application-env

log "Setting up PostgreSQL"
# Ensure PostgreSQL is running
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Setup PostgreSQL
sudo -u postgres psql << EOF
-- Create user if not exists
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USERNAME}') THEN
        CREATE USER "${DB_USERNAME}" WITH PASSWORD '${DB_PASSWORD}';
    END IF;
END
\$\$;

-- Create database if not exists
SELECT 'CREATE DATABASE "${DB_NAME}"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')
\gexec

-- Grant privileges and alter user
GRANT ALL PRIVILEGES ON DATABASE "${DB_NAME}" TO "${DB_USERNAME}";
ALTER USER "${DB_USERNAME}" WITH SUPERUSER;

\du
EOF

log "Displaying PostgreSQL user information"
sudo -u postgres psql -c '\du'

log "Verifying database creation"
if sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw "${DB_NAME}"; then
    log "Database ${DB_NAME} created successfully"
else
    log "Error: Database ${DB_NAME} was not created"
    exit 1
fi

log "Setup completed successfully"