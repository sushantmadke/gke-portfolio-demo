# On-demand pool: baseline capacity, autoscales at the node-pool level based on
# aggregate pod resource requests (Cluster Autoscaler) - distinct from the HPA's
# pod-level scaling in charts/orderflow/templates/hpa.yaml.
resource "google_container_node_pool" "default_pool" {
  name     = "default-pool"
  cluster  = google_container_cluster.primary.id
  location = var.zone

  autoscaling {
    min_node_count = 1
    max_node_count = 3
  }

  management {
    auto_repair  = true
    auto_upgrade = true
  }

  node_config {
    machine_type = "e2-small"
    disk_size_gb = 30
    disk_type    = "pd-standard"

    workload_metadata_config {
      mode = "GKE_METADATA"
    }

    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform",
    ]

    labels = {
      pool = "default"
    }
  }
}

# Spot pool: cost-optimized burst capacity for load-test spikes. min_node_count = 0
# means it scales to zero when idle instead of holding pre-paid capacity.
resource "google_container_node_pool" "spot_pool" {
  name     = "spot-pool"
  cluster  = google_container_cluster.primary.id
  location = var.zone

  autoscaling {
    min_node_count = 0
    max_node_count = 5
  }

  management {
    auto_repair  = true
    auto_upgrade = true
  }

  node_config {
    spot         = true
    machine_type = "e2-small"
    disk_size_gb = 30
    disk_type    = "pd-standard"

    workload_metadata_config {
      mode = "GKE_METADATA"
    }

    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform",
    ]

    labels = {
      pool = "spot"
    }

    # GKE auto-applies the cloud.google.com/gke-spot=true:NoSchedule taint to spot
    # nodes; no explicit taint block needed here (adding one duplicates it).
  }
}
