#!/bin/sh

BASE=$(dirname "$0")

mkdir -p "$BASE/../build/dist/"
rm "$BASE/../build/pdx_unlimiter-mac_intel.dmg"

create-dmg \
  --volname "Pdx-Unlimiter" \
  --volicon "$BASE/logo.icns" \
  --window-pos 200 120 \
  --window-size 512 228 \
  --icon-size 128 \
  --icon "Pdx-Unlimiter.app" 64 50 \
  --app-drop-link 320 50 \
  "$BASE/../build/pdx_unlimiter-mac_intel.dmg" \
  "$BASE/../build/dist/Pdx-Unlimiter.app"
