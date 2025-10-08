#!/bin/bash

set -e

sudo -S "/Applications/$1.app/Contents/Resources/scripts/uninstall.sh" || true
sudo -S installer -verboseR -allowUntrusted -pkg "$2" -target /