data "google_project" "current" {}

resource "google_pubsub_topic" "orders" {
  name = "orders"
}

resource "google_pubsub_topic" "orders_dlq" {
  name = "orders-dlq"
}

resource "google_pubsub_subscription" "orders_worker" {
  name  = "orders-worker-sub"
  topic = google_pubsub_topic.orders.name

  ack_deadline_seconds = 30

  retry_policy {
    minimum_backoff = "5s"
    maximum_backoff = "60s"
  }

  # After 5 failed delivery attempts (order-worker nacks), Pub/Sub stops retrying
  # against this subscription and republishes the message to orders-dlq instead.
  dead_letter_policy {
    dead_letter_topic     = google_pubsub_topic.orders_dlq.id
    max_delivery_attempts = 5
  }
}

# The Pub/Sub service agent needs explicit permission to republish into the DLQ topic
# and to pull from the source subscription on the dead-lettering path.
resource "google_pubsub_topic_iam_member" "dlq_publisher" {
  topic  = google_pubsub_topic.orders_dlq.name
  role   = "roles/pubsub.publisher"
  member = "serviceAccount:service-${data.google_project.current.number}@gcp-sa-pubsub.iam.gserviceaccount.com"
}

resource "google_pubsub_subscription_iam_member" "dlq_subscriber" {
  subscription = google_pubsub_subscription.orders_worker.name
  role         = "roles/pubsub.subscriber"
  member       = "serviceAccount:service-${data.google_project.current.number}@gcp-sa-pubsub.iam.gserviceaccount.com"
}
