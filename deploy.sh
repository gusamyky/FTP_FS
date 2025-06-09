#!/bin/bash

# Azure Container Registry name
ACR_NAME="ftpfsacr"
# Container App name
APP_NAME="ftpfs-app"
# Resource Group name
RESOURCE_GROUP="student-mysql-rg"
# Container Apps Environment name
ENV_NAME="ftpfs-env"
# Location
LOCATION="westeurope"

# Login to Azure Container Registry
echo "Logging in to Azure Container Registry..."
az acr login --name $ACR_NAME

# Build and push the Docker image
echo "Building and pushing Docker image..."
docker build --platform linux/amd64 -t $ACR_NAME.azurecr.io/ftp-server:latest .
docker push $ACR_NAME.azurecr.io/ftp-server:latest

# Get the ACR credentials
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query "username" -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv)

# Deploy to Container App
echo "Deploying to Container App..."
az containerapp update \
  --name $APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_NAME.azurecr.io/ftp-server:latest \
  --set-env-vars \
    SERVER_PORT=2121 \
    SERVER_HOST=0.0.0.0 \
    SERVER_FILES_DIR=/app/server_files \
    DB_URL="jdbc:mysql://ftp-fs.mysql.database.azure.com:3306/ftp_fs?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    DB_USER="gusamyky" \
    DB_PASSWORD="dysmex-wyBdod-nydfe7"

echo "Deployment completed!" 