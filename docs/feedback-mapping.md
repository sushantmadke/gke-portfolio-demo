# Feedback → evidence mapping

Original feedback: *"Sushant successfully deploys services to GKE with autoscaling,
monitors pod-level logs, manages health checks, and adjusts consumer acknowledgment
strategies. However, he lacks evidence of designing complex container workflows or
performing advanced cluster operations. Additionally, his background shows no proof
of building CI/CD pipelines or managing cluster-level scaling policies."*

Repo: `gke-portfolio-demo` (link once pushed to GitHub).

## 1. Complex container workflows

- Multi-stage Dockerfiles with a Maven build stage and a minimal non-root JRE runtime
  stage, using Spring Boot layered jars for better layer caching:
  [`order-api/Dockerfile`](../order-api/Dockerfile), [`order-worker/Dockerfile`](../order-worker/Dockerfile)
- Helm chart with per-environment values (`values-dev.yaml` / `values-prod.yaml`)
  driving replica counts, autoscaling bounds, and image tags from one template set:
  [`charts/orderflow/`](../charts/orderflow/)
- [ ] TODO after deploy: paste `docker history` output or image size comparison
  (build-stage image vs. final runtime image) into `artifacts/`.

## 2. Advanced cluster operations

- VPC-native cluster with dedicated network/subnet and secondary IP ranges for
  pods/services: [`infra/network.tf`](../infra/network.tf), [`infra/gke.tf`](../infra/gke.tf)
- Workload Identity for pod-level GCP auth (no mounted key files):
  [`infra/workload-identity.tf`](../infra/workload-identity.tf), [`charts/orderflow/templates/serviceaccount.yaml`](../charts/orderflow/templates/serviceaccount.yaml)
- Namespaced RBAC scoping the CI deployer identity to least-privilege verbs on a
  fixed resource set, no delete/secrets access:
  [`charts/orderflow/templates/rbac.yaml`](../charts/orderflow/templates/rbac.yaml)
- NetworkPolicy (default-deny + explicit allow), PodDisruptionBudget,
  ResourceQuota/LimitRange: [`charts/orderflow/templates/`](../charts/orderflow/templates/)
- Pub/Sub dead-letter routing with IAM bindings for the Pub/Sub service agent:
  [`infra/pubsub.tf`](../infra/pubsub.tf)
- [ ] TODO after deploy: `kubectl auth can-i --as=<ci-deployer-email> delete pods -n orderflow-dev`
  showing `no`, captured into `artifacts/rbac-check.txt`.

## 3. CI/CD pipelines

- `.github/workflows/ci-cd.yaml`: test → build → Trivy scan → push to Artifact
  Registry → Helm deploy, branch-gated (`develop` → dev, `main` → prod behind a
  GitHub Environment approval).
- `.github/workflows/terraform.yaml`: `fmt`/`validate`/`plan` on every infra PR,
  manual-approval `apply` via `workflow_dispatch`.
- Keyless auth throughout via Workload Identity Federation — no long-lived GCP keys
  stored in GitHub.
- [ ] TODO after first real run: link to a green Actions run, screenshot the run
  summary into `artifacts/`.

## 4. Cluster-level scaling policies

- Cluster Autoscaler configured per node pool in Terraform, not just pod-level HPA:
  [`infra/node-pools.tf`](../infra/node-pools.tf) — `default-pool` (1-3 on-demand
  nodes) and `spot-pool` (0-5 spot nodes, scales to zero when idle).
- Distinct from the pod-level `HorizontalPodAutoscaler`s already in place:
  [`charts/orderflow/templates/hpa.yaml`](../charts/orderflow/templates/hpa.yaml).
- [ ] TODO after load test: `kubectl get nodes -w` and `kubectl get hpa -w` output
  plus GCP Console screenshots showing node count increasing under load, saved to
  `artifacts/load-test/`.

## 5. Artifacts / proofs

- This repository itself, with a real commit history and PRs.
- `artifacts/` — load-test results, scaling screenshots, `kubectl` command output,
  GitHub Actions run links, Terraform plan/apply output.
