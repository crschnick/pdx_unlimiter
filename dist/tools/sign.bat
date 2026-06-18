@echo off

AzureSignTool sign --skip-signed -kvu "%AZURE_KEY_VAULT_URI%" -kvi "%AZURE_CLIENT_ID%" -kvt "%AZURE_TENANT_ID%" -kvs "%AZURE_CLIENT_SECRET%" -kvc "%AZURE_CERT_NAME%" -tr http://timestamp.globalsign.com/tsa/r45standard -v %*
exit
