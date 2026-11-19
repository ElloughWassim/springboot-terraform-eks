terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  # Remote state in S3 with native S3 locking. Values are supplied at init time so that
  # no account-specific data lives in Git:
  #   terraform init -backend-config=backend.hcl
  # See backend.hcl.example.
  backend "s3" {}
}
