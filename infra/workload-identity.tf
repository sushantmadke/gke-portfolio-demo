# ---- Runtime identity: order-api / order-worker pods call Pub/Sub as this GSA ----

resource "google_service_account" "workload" {
  account_id   = "orderflow-workload"
  display_name = "orderflow pod runtime identity (Workload Identity)"
}

resource "google_pubsub_topic_iam_member" "workload_publisher" {
  topic  = google_pubsub_topic.orders.name
  role   = "roles/pubsub.publisher"
  member = "serviceAccount:${google_service_account.workload.email}"
}

resource "google_pubsub_subscription_iam_member" "workload_subscriber" {
  subscription = google_pubsub_subscription.orders_worker.name
  role         = "roles/pubsub.subscriber"
  member       = "serviceAccount:${google_service_account.workload.email}"
}

# Binds the Kubernetes ServiceAccount (charts/orderflow/templates/serviceaccount.yaml)
# to this GSA. One binding per environment namespace the chart is deployed into.
resource "google_service_account_iam_member" "workload_identity_binding" {
  for_each           = toset(["orderflow-dev", "orderflow-prod"])
  service_account_id = google_service_account.workload.name
  role                = "roles/iam.workloadIdentityUser"
  member              = "serviceAccount:${var.project_id}.svc.id.goog[${each.value}/orderflow-workload]"
}

# ---- CI identity: GitHub Actions builds/pushes/deploys as this GSA, no JSON keys ----

resource "google_service_account" "ci_deployer" {
  account_id   = "orderflow-ci-deployer"
  display_name = "GitHub Actions CI/CD deployer"
}

resource "google_project_iam_member" "ci_deployer_gke" {
  project = var.project_id
  role    = "roles/container.developer"
  member  = "serviceAccount:${google_service_account.ci_deployer.email}"
}

resource "google_artifact_registry_repository_iam_member" "ci_deployer_ar" {
  repository = google_artifact_registry_repository.orderflow.name
  location   = google_artifact_registry_repository.orderflow.location
  role       = "roles/artifactregistry.writer"
  member     = "serviceAccount:${google_service_account.ci_deployer.email}"
}

resource "google_iam_workload_identity_pool" "github" {
  workload_identity_pool_id = "github-actions-pool"
  display_name              = "GitHub Actions"
  description                = "Federates GitHub Actions OIDC tokens for keyless deploys"
}

resource "google_iam_workload_identity_pool_provider" "github" {
  workload_identity_pool_id         = google_iam_workload_identity_pool.github.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-actions-provider"
  display_name                       = "GitHub Actions OIDC"

  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.repository" = "assertion.repository"
    "attribute.ref"        = "assertion.ref"
  }

  # Restricts token exchange to this exact repo - no other GitHub repo can mint
  # tokens for orderflow-ci-deployer even if it discovers the pool/provider IDs.
  attribute_condition = "assertion.repository == \"${var.github_repo}\""

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account_iam_member" "ci_deployer_wif_binding" {
  service_account_id = google_service_account.ci_deployer.name
  role                = "roles/iam.workloadIdentityUser"
  member              = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github.name}/attribute.repository/${var.github_repo}"
}
