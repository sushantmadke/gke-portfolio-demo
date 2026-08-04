output "cluster_name" {
  value = google_container_cluster.primary.name
}

output "cluster_zone" {
  value = var.zone
}

output "artifact_registry_repo" {
  value = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.orderflow.repository_id}"
}

output "ci_deployer_service_account" {
  value = google_service_account.ci_deployer.email
}

# Feed this into the GitHub Actions workflow's `workload_identity_provider` input
# (google-github-actions/auth) for keyless CI authentication.
output "workload_identity_provider" {
  value = google_iam_workload_identity_pool_provider.github.name
}
