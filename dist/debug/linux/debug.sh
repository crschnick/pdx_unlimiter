#!/bin/bash

DIR="${0%/*}"
EXTRA_ARGS=(__JVM_ARGS__)
export CDS_JVM_OPTS="${EXTRA_ARGS[*]}"

"$DIR/../lib/runtime/bin/__EXECUTABLE_NAME__" "$@"

read -rsp "Press any key to close" -n 1 key
