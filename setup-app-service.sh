#!/bin/bash

# Variables
RESOURCE_GROUP="student-mysql-rg"
APP_NAME="ftpfs-app-service"
LOCATION="westeurope"
PLAN_NAME="ftpfs-plan"
SKU="B1"  # Basic tier, can be adjusted based on needs

# Create App Service Plan
echo "Creating App Service Plan..."
az appservice plan create \
    --name $PLAN_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --sku $SKU \
    --is-linux

# Create Web App
echo "Creating Web App..."
az webapp create \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --plan $PLAN_NAME \
    --runtime "JAVA:17-java17"

# Configure app settings
echo "Configuring app settings..."
az webapp config appsettings set \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --settings \
    SERVER_PORT=2121 \
    SERVER_HOST=0.0.0.0 \
    SERVER_FILES_DIR=/home/site/wwwroot/server_files \
    DB_URL="jdbc:mysql://YOUR_DB_SERVER:3306/YOUR_DB_NAME" \
    DB_USER="YOUR_DB_USER" \
    DB_PASSWORD="YOUR_DB_PASSWORD" \
    DB_URL_NO_DB="jdbc:mysql://YOUR_DB_SERVER:3306" \
    SSL_KEYSTORE=/home/site/wwwroot/keystore.jks \
    SSL_KEYSTORE_PASS="YOUR_KEYSTORE_PASSWORD"

# Configure FTP
echo "Configuring FTP..."
az webapp config set \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --ftps-state FtpsOnly

# Get the publish profile
echo "Getting publish profile..."
az webapp deployment list-publishing-profiles \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --xml > publish_profile.xml

echo "Setup completed! The publish profile has been saved to publish_profile.xml"
echo "Please add this profile as a secret named AZURE_WEBAPP_PUBLISH_PROFILE in your GitHub repository" 