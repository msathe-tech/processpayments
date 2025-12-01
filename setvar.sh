# --- REQUIRED: Set these variables for your environment ---
export GCP_PROJECT_ID=$(gcloud config get-value project)
export GCP_REGION="us-central1"
export GKE_CLUSTER_NAME="kafka-streams-cluster"
export AR_REPO_NAME="app-images" # Artifact Registry repository name
export APP_NAME="processpayments"

# --- These variables will be set based on the ones above ---
export GSA_NAME="${APP_NAME}-gsa"
export KSA_NAME="${APP_NAME}-sa"
export GSA_EMAIL="${GSA_NAME}@${GCP_PROJECT_ID}.iam.gserviceaccount.com"
export AR_HOST="${GCP_REGION}-docker.pkg.dev"
export IMAGE_TAG="${AR_HOST}/${GCP_PROJECT_ID}/${AR_REPO_NAME}/${APP_NAME}:latest"

