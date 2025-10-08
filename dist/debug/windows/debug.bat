@echo off
set CDS_JVM_OPTS=__JVM_ARGS__
chcp 65001 > NUL
CALL "%~dp0\..\runtime\bin\__EXECUTABLE_NAME__.bat" %*
pause
