#!/bin/bash

# Only run on full uninstall
if [ $1 -lt 1 ] ; then
  xdg-desktop-menu uninstall __TARGET__/__PACKAGE__.desktop || true
fi

killall __TARGET__/bin/__EXECUTABLE_NAME__ --signal SIGTERM 2>/dev/null || true