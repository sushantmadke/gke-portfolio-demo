resource "google_artifact_registry_repository" "orderflow" {
  repository_id = "orderflow"
  location      = var.region
  format        = "DOCKER"
  description   = "order-api / order-worker container images"
}
