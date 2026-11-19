# Credentials are never configured here. The AWS provider reads them from the
# environment (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, AWS_PROFILE, an instance
# role or OIDC web identity), which keeps secrets out of variables, state and logs.
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(
      {
        Project   = var.project_name
        ManagedBy = "terraform"
      },
      var.tags
    )
  }
}
