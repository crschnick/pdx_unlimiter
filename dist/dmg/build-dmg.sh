#!/bin/bash

ARCH="$2"
NAME="$3"
APP_DIR="$1/build/dist/$NAME.app"
DMG_FILE="$1/build/dist/dmg/$4"

mkdir -p "$1/build/dist/dmg/"
rm -f "$DMG_FILE"

# This is unreliable, so retry
for i in {1..5} ; do
  # The --skip-jenkins option should be required for CI systems, but on some runners it works without it
  # Without the option, the dmg looks better
  create-dmg \
    --volname "$NAME" \
    --volicon "$1/logo/logo.icns" \
    --background "$1/dmg/background.png" \
    --window-pos 200 120 \
    --window-size 450 200 \
    --icon-size 100 \
    --icon "$NAME.app" 100 55 \
    --hide-extension "$NAME.app" \
    --app-drop-link 300 50 \
    --codesign "$MACOS_DEVELOPER_ID_APPLICATION_CERTIFICATE_NAME" \
    "$DMG_FILE" \
    "$APP_DIR"
    if [ $? == 0 ]; then
        break;
    fi
done

if [ $? != 0 ]; then
    exit 1
fi