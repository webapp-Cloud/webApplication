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
#     filters = {
#       name                = "ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"
#       root-device-type    = "ebs"
#       virtualization-type = "hvm"
#     }//ghgh
    most_recent = true
    owners      = ["099720109477"] # Canonical's AWS account ID
  }

  tags = {
    Name        = "CSYE6225 AMI"
    Environment = "Development"
    Course      = "CSYE6225"
    Created     = timestamp()
  }

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }
}

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


  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install -y dos2unix",
      "dos2unix /tmp/setup_system.sh",
      "dos2unix /tmp/setup_application.sh"
    ]
  }
  # Execute setup scripts
  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup_system.sh",
      "sudo /tmp/setup_system.sh",
      "chmod +x /tmp/setup_application.sh",
      "sudo /tmp/setup_application.sh"
    ]
  }

  # Verify installation
  provisioner "shell" {
    inline = [
      "echo 'Verifying installation...'",
      "java -version",
      "sudo systemctl status csye6225 || true",
      "ls -la /opt/csye6225",
      "sudo cat /etc/systemd/system/csye6225.service"
    ]
  }
}
