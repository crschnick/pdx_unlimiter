#!/bin/sh

set -e

APP_DIR="$1/build/dist/Pdx-Unlimiter.app"
DMG_FILE="$1/build/pdx_unlimiter-macos-x86_64.dmg"

echo "$DMG_FILE"
mkdir -p "$1/build/dist/"
rm -f "$DMG_FILE"

create-dmg \
  --volname "Pdx-Unlimiter" \
  --volicon "$1/misc/logo.icns" \
  --window-pos 200 120 \
  --window-size 512 228 \
  --icon-size 128 \
  --icon "Pdx-Unlimiter.app" 64 50 \
  --app-drop-link 320 50 \
  --codesign "Developer ID Application: Christopher Schnick (PF6V9HYACS)" \
  "$DMG_FILE" \
  "$APP_DIR"

