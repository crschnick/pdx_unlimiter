CALL "C:\Program Files\Microsoft Visual Studio\2022\Enterprise\VC\Auxiliary\Build\vcvars64.bat" || CALL "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
AzureSignTool sign --skip-signed -kvu "%AZURE_KEY_VAULT_URI%" -kvi "%AZURE_CLIENT_ID%" -kvt "%AZURE_TENANT_ID%" -kvs "%AZURE_CLIENT_SECRET%" -kvc "%AZURE_CERT_NAME%" -tr http://timestamp.globalsign.com/tsa/r45standard -v %*
exit
