output "cluster_name" {
  description = "EKS cluster name (used by `aws eks update-kubeconfig`)."
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "Kubernetes API server endpoint."
  value       = aws_eks_cluster.this.endpoint
}

output "cluster_version" {
  description = "Kubernetes version of the control plane."
  value       = aws_eks_cluster.this.version
}

output "ecr_repository_url" {
  description = "URL of the application ECR repository (empty when not created)."
  value       = var.create_ecr_repository ? aws_ecr_repository.app[0].repository_url : ""
}

output "update_kubeconfig_command" {
  description = "Command that configures kubectl for this cluster."
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${aws_eks_cluster.this.name}"
}
