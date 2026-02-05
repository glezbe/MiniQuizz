# Change these four parameters as needed
ACI_PERS_RESOURCE_GROUP=grupJG
ACI_PERS_STORAGE_ACCOUNT_NAME=mystorageaccount$RANDOM
ACI_PERS_LOCATION=westeurope
ACI_PERS_SHARE_NAME=adud5a3_mvc_cine

# Create the storage account with the parameters
az storage account create \
    --resource-group $ACI_PERS_RESOURCE_GROUP \
    --name $ACI_PERS_STORAGE_ACCOUNT_NAME \
    --location $ACI_PERS_LOCATION \
    --sku Standard_LRS

# Create the file share
az storage share create \
  --name $ACI_PERS_SHARE_NAME \
  --account-name $ACI_PERS_STORAGE_ACCOUNT_NAME


# STORAGE_KEY=$(az storage account keys list --resource-group $ACI_PERS_RESOURCE_GROUP --account-name $ACI_PERS_STORAGE_ACCOUNT_NAME --query "[0].value" --output tsv)
# echo $STORAGE_KEY


Creació d'un volum des del context docker

docker --context aci volume create test-volume --storage-account mystorageaccount
[+] Running 2/2
 ⠿ mystorageaccount  Created                         26.2s
 ⠿ test-volume       Created                          0.9s
mystorageaccount/test-volume

