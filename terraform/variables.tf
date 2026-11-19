variable "aws_region" {
  description = "AWS region where the cluster is created."
  type        = string
  default     = "eu-west-1"
}

variable "project_name" {
  description = "Prefix used for resource names and tags."
  type        = string
  default     = "springboot-demo"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{2,30}$", var.project_name))
    error_message = "project_name must be 3-31 lowercase letters, digits or dashes."
  }
}

variable "cluster_name" {
  description = "Name of the EKS cluster."
  type        = string
  default     = "springboot-demo-eks"
}

variable "kubernetes_version" {
  description = "EKS Kubernetes minor version. Check the EKS release calendar before changing it."
  type        = string
  default     = "1.36"
}

variable "subnet_ids" {
  description = "IDs of at least two existing private subnets in different Availability Zones."
  type        = list(string)

  validation {
    condition     = length(var.subnet_ids) >= 2
    error_message = "EKS requires at least two subnets in different Availability Zones."
  }
}

variable "endpoint_public_access" {
  description = "Expose the Kubernetes API endpoint publicly. Keep false when Jenkins runs inside the VPC (secure default)."
  type        = bool
  default     = false
}

variable "endpoint_public_access_cidrs" {
  description = "CIDR blocks allowed to reach the public API endpoint when endpoint_public_access = true (for example the Jenkins agent IP)."
  type        = list(string)
  default     = []

  validation {
    condition     = !contains(var.endpoint_public_access_cidrs, "0.0.0.0/0")
    error_message = "Do not open the Kubernetes API to 0.0.0.0/0; list specific CIDR blocks."
  }
}

variable "node_instance_types" {
  description = "EC2 instance types for the managed node group."
  type        = list(string)
  default     = ["t3.medium"]
}

variable "node_desired_size" {
  description = "Desired number of worker nodes."
  type        = number
  default     = 2
}

variable "node_min_size" {
  description = "Minimum number of worker nodes."
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Maximum number of worker nodes."
  type        = number
  default     = 3
}

variable "create_ecr_repository" {
  description = "Create an ECR repository for the application image."
  type        = bool
  default     = true
}

variable "ecr_repository_name" {
  description = "Name of the ECR repository (must match the image name used by the pipeline)."
  type        = string
  default     = "springboot-terraform-eks"
}

variable "tags" {
  description = "Additional tags applied to every resource."
  type        = map(string)
  default     = {}
}
