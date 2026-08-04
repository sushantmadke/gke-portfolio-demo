terraform {
  required_version = ">= 1.7.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.40"
    }
  }

  # Local backend by default for a portfolio demo. For anything longer-lived,
  # swap this for a "gcs" backend pointing at a versioned bucket.
}

provider "google" {
  project = var.project_id
  region  = var.region
}
