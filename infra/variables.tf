variable "project_id" {
  type        = string
  description = "GCP project ID the demo runs in."
}

variable "region" {
  type        = string
  description = "Region for regional resources (Artifact Registry, subnet)."
  default     = "us-central1"
}

variable "zone" {
  type        = string
  description = "Zone for the GKE cluster (zonal cluster keeps cost down for a demo)."
  default     = "us-central1-a"
}

variable "cluster_name" {
  type    = string
  default = "orderflow-demo"
}

variable "github_repo" {
  type        = string
  description = "GitHub repo allowed to assume the CI deployer identity, as \"owner/repo\"."
}

variable "environment" {
  type        = string
  description = "Short environment tag applied as a resource label."
  default     = "demo"
}
