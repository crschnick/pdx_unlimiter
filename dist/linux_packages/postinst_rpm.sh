#!/bin/bash

xdg-desktop-menu install --novendor __TARGET__/__PACKAGE__.desktop || true
mandb > /dev/null 2>&1 || true
