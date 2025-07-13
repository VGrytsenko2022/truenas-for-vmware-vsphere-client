@echo off

set JAR_NAME=truenas-for-vmware-vsphere-client-1.0-SNAPSHOT.jar
set REMOTE_USER=root
set REMOTE_HOST=dev-01.dit.lab
set REMOTE_DIR=/root/truenas-for-vmware-vsphere-client

echo [INFO] Kill old process...
ssh %REMOTE_USER%@%REMOTE_HOST% "pkill -f '%JAR_NAME%' > app.log 2>&1 &"

echo [INFO] Copy %JAR_NAME% to %REMOTE_HOST%:%REMOTE_DIR%
scp target\%JAR_NAME% %REMOTE_USER%@%REMOTE_HOST%:%REMOTE_DIR%



echo [INFO] Run JAR on server...
ssh %REMOTE_USER%@%REMOTE_HOST% "cd %REMOTE_DIR% && nohup java -jar %JAR_NAME% > app.log 2>&1 &"
