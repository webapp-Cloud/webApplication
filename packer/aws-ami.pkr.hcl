packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0 , < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  ami_name      = "csye6225-ami-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  instance_type = "t2.micro"
  region        = "us-east-1"
  # source_ami = "ami-0866a3c8686eaeeba"
  source_ami_filter{
    filters = {
      name = "ubuntu/images/*ubuntu-jammy-22.04-amd64-server-*"
      root-device-type = "ebs"
      virtualization-type = "hvm"
    }

    most_recent = true
    owners = ["099720109477"]
  }

  ssh_username = "ubuntu"
  ami_users = [039612889197]
  vpc_id = "vpc-02d1996bd241fd317"
}


build {
  name    = "csye6225-packer-ami"
  sources = [
    "source.amazon-ebs.ubuntu"
  ]
  provisioner "shell" {
    inline = [
      "echo 'Printing system information:'",
      "cat /etc/os-release",
      "uname -a",
      "which apt-get || echo 'apt-get not found'",
      "sudo apt-get update"
    ]
  }

  provisioner "shell" {
    script = "setup.sh"
  }
}