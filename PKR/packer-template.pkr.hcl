packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# Variable definitions
variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-0866a3c8686eaeeba" # Ubuntu 22.04 LTS AMI ID
}

variable "instance_type" {
  type    = string
  default = "t2.micro"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = null # Will use default subnet if not specified
}

variable "ami_users" {
  type    = list(string)
  default = ["039612889197"] # Empty list by default
}

variable "ami_name_prefix" {
  type    = string
  default = "csye6225"
}

# Source block
source "amazon-ebs" "ubuntu" {
  region          = var.aws_region
  instance_type   = var.instance_type
  ssh_username    = var.ssh_username
  ami_name        = "${var.ami_name_prefix}-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  ami_description = "Ubuntu AMI for CSYE6225 with Java application"
  ami_users       = var.ami_users

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"] # Canonical's AWS account ID
  }
  #   this is for new source ami
  tags = {
    Name        = "CSYE6225 AMI"
    Environment = "Development"
    Course      = "CSYE6225"
    Created     = timestamp()
    AutoScaling = "true"
  }

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }
}
# buildiing block
# Build block
build {
  name    = "csye6225-ami"
  sources = ["source.amazon-ebs.ubuntu"]

  # Copy application files
  provisioner "file" {
    source      = "./../target/cloud-csye-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp.jar"
  }

  provisioner "file" {
    source      = "setup_system.sh"
    destination = "/tmp/setup_system.sh"
  }

  provisioner "file" {
    source      = "setup_application.sh"
    destination = "/tmp/setup_application.sh"
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/csye6225.service"
  }
  provisioner "file" {
    source      = "amazon-cloudwatch-agent.json"
    destination = "/tmp/amazon-cloudwatch-agent.json"
  }
  # Execute setup scripts
  #   provisioner "shell" {
  #     inline = [
  #       "chmod +x /tmp/setup_system.sh",
  #       "sudo /tmp/setup_system.sh",
  #       "chmod +x /tmp/setup_application.sh",
  #       "sudo /tmp/setup_application.sh",
  #       "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
  #       #
  #       "sudo chmod 644 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
  #       "sudo chown root:root /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
  #       #
  #       "sudo systemctl enable amazon-cloudwatch-agent",
  #       #
  #       # Verify CloudWatch agent configuration
  #       "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a verify -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json"
  #       #
  #     ]
  #   }

  # Execute setup scripts
  provisioner "shell" {
    inline = [
      "echo '=== Starting system setup ==='",
      "chmod +x /tmp/setup_system.sh",
      "sudo /tmp/setup_system.sh",

      "echo '=== Verifying system setup ==='",
      "id csye6225 || (echo 'User creation failed' && exit 1)",
      "getent group csye6225 || (echo 'Group creation failed' && exit 1)",
      "[ -d /opt/csye6225 ] || (echo 'Directory creation failed' && exit 1)",

      "echo '=== Starting application setup ==='",
      "chmod +x /tmp/setup_application.sh",
      "sudo /tmp/setup_application.sh",

      "echo '=== Configuring CloudWatch agent ==='",
      "sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc",
      "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo chown root:root /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo chmod 644 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",

      "echo '=== Verifying CloudWatch agent configuration ==='",
      "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s",
      "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a status",


      "echo '=== Enabling services ==='",
      "sudo systemctl enable csye6225.service",
      "sudo systemctl enable amazon-cloudwatch-agent.service"
    ]
  }

  # Verify installation
  provisioner "shell" {
    inline = [
      "echo 'Verifying installation...'",
      "java -version",
      "sudo systemctl status csye6225 || true",
      "sudo systemctl status amazon-cloudwatch-agent || true",
      "ls -la /opt/csye6225",
      "sudo cat /etc/systemd/system/csye6225.service"
    ]
  }
}


