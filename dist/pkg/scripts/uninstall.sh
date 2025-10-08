#!/bin/bash

DATE=`date +%Y-%m-%d`
TIME=`date +%H:%M:%S`
LOG_PREFIX="[$DATE $TIME]"
INSTALLPATH="/Applications/"

if (( $EUID != 0 )); then
    echo "This scripts need to be run as root."
    exit
fi

echo "Uninstalling __PRODUCT__ ..."

killall -SIGTERM __EXECUTABLE_NAME__ 2>/dev/null || true

VERSION="__VERSION__"
PRODUCT="__PRODUCT__"
PRODUCT_KEBAP="__PRODUCT_KEBAP__"
PACKAGE_ID="__PACKAGE_ID__"

find "/usr/local/bin/" -name "${PRODUCT_KEBAP}" | xargs rm
if [ $? -eq 0 ]
then
  echo "[1/3] [DONE] Successfully deleted shortcut links"
else
  echo "[1/3] [ERROR] Could not delete shortcut links" >&2
fi

pkgutil --forget "${PACKAGE_ID}" > /dev/null 2>&1
if [ $? -eq 0 ]
then
  echo "[2/3] [DONE] Successfully deleted application information"
else
  echo "[2/3] [ERROR] Could not delete application information" >&2
fi


[ -e "${INSTALLPATH}/${PRODUCT}.app" ] && rm -rf "${INSTALLPATH}/${PRODUCT}.app"
if [ $? -eq 0 ]
then
  echo "[3/3] [DONE] Successfully deleted application"
else
  echo "[3/3] [ERROR] Could not delete application" >&2
fi

echo "${PRODUCT} ${VERSION} has been successfully uninstalled!"
exit 0
