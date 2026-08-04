resource "google_container_cluster" "primary" {
  name     = var.cluster_name
  location = var.zone

  network    = google_compute_network.vpc.id
  subnetwork = google_compute_subnetwork.subnet.id

  # Node pools are managed as separate google_container_node_pool resources
  # (see node-pools.tf) so each can carry its own machine type and autoscaling policy.
  remove_default_node_pool = true
  initial_node_count       = 1

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods"
    services_secondary_range_name = "services"
  }

  workload_identity_config {
    workload_pool = "${var.project_id}.svc.id.goog"
  }

  release_channel {
    channel = "REGULAR"
  }

  # Public endpoint (no master_authorized_networks_config) so kubectl/Helm from
  # GitHub Actions' hosted runners can reach it without a bastion. Authorization is
  # enforced by IAM + the namespaced RBAC in charts/orderflow/templates/rbac.yaml,
  # not by network reachability.
  resource_labels = {
    environment = var.environment
    project     = "orderflow"
  }
}
