
packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0 , < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "vpc_id" {
  type = string
}

variable "ami_users" {
  type = list(string)
}

variable "db_url" {
  type = string
}

variable "db_username" {
  type = string
}

variable "db_password" {
  type = string
}

source "amazon-ebs" "ubuntu" {
  ami_name      = "csye6225-ami-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  instance_type = "t2.medium"
  region        = var.aws_region
  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"]
  }
  ssh_username = "ubuntu"
  ami_users    = var.ami_users
  vpc_id       = var.vpc_id
}

build {
  name = "csye6225-packer-ami"
  sources = [
    "source.amazon-ebs.ubuntu"
  ]

  provisioner "shell" {
    script = "setup.sh"
    environment_vars = [
      "DB_URL=${var.db_url}",
      "DB_USERNAME=${var.db_username}",
      "DB_PASSWORD=${var.db_password}"
    ]
  }
  
  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install -y gnupg",
      "sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 3B4FE6ACC0B21F32",
      "sudo apt-get update",
      "echo 'Printing system information:'",
      "cat /etc/os-release",
      "uname -a"
    ]
  }

  provisioner "file" {
    source      = "./../target/cloud-csye-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/cloud-csye6225-rishabh.jar"
  }
  
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/cloud-csye6225-rishabh.jar /opt/csye6225/app/",
      "sudo chown csye6225:csye6225 /opt/csye6225/app/cloud-csye6225-rishabh.jar",
      "sudo chmod 500 /opt/csye6225/app/cloud-csye6225-rishabh.jar"
    ]
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/csye6225.service"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/csye6225.service /etc/systemd/system/csye6225.service",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable csye6225.service"
    ]
  }
}