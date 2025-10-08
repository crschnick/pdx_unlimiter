powershell -Command "$app = Get-WmiObject -Class Win32_Product | Where-Object { $_.Name -eq '%~1' }; if ($app -ne $null) {$app.Uninstall()}"
start /wait msiexec /i "%~2" /quiet
