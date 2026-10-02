#!/usr/bin/env bash

# Usage:
#   ./update_rakaly.sh            # install the latest release
#   ./update_rakaly.sh v0.8.20    # install a specific release tag

set -euo pipefail

REPO="rakaly/cli"
TAG="${1:-latest}"

command -v curl >/dev/null || { echo "curl is required" >&2; exit 1; }
command -v tar >/dev/null || { echo "tar is required" >&2; exit 1; }
command -v unzip >/dev/null || { echo "unzip is required" >&2; exit 1; }

REPO_ROOT=$(git rev-parse --show-toplevel)
RAKALY_DIR="$REPO_ROOT/rakaly"
WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

if [ "$TAG" = "latest" ]; then
    API_URL="https://api.github.com/repos/$REPO/releases/latest"
else
    API_URL="https://api.github.com/repos/$REPO/releases/tags/$TAG"
fi

echo "Fetching release metadata from $API_URL"
RELEASE_JSON=$(curl -sfL "$API_URL")

RESOLVED_TAG=$(printf '%s' "$RELEASE_JSON" | grep -m1 '"tag_name"' | sed -E 's/.*"tag_name": *"([^"]+)".*/\1/')
if [ -z "$RESOLVED_TAG" ]; then
    echo "Could not resolve a release tag for $REPO ($TAG)" >&2
    exit 1
fi
VERSION="${RESOLVED_TAG#v}"
echo "Resolved release: $RESOLVED_TAG"

BASE_URL="https://github.com/$REPO/releases/download/$RESOLVED_TAG"

for platform in windows linux mac; do
    case "$platform" in
        windows)
            asset="rakaly-$VERSION-x86_64-pc-windows-msvc.zip"
            output_name="rakaly_windows.exe"
            ;;
        linux)
            asset="rakaly-$VERSION-x86_64-unknown-linux-musl.tar.gz"
            output_name="rakaly_linux"
            ;;
        mac)
            asset="rakaly-$VERSION-x86_64-apple-darwin.tar.gz"
            output_name="rakaly_mac"
            ;;
    esac

    url="$BASE_URL/$asset"
    archive="$WORK_DIR/$asset"

    echo "Downloading $asset"
    curl -sfL -o "$archive" "$url"

    extract_dir="$WORK_DIR/$platform"
    mkdir -p "$extract_dir"

    case "$asset" in
        *.zip)
            unzip -q "$archive" -d "$extract_dir"
            extracted_bin=$(find "$extract_dir" -type f -name 'rakaly.exe')
            ;;
        *.tar.gz)
            tar -xzf "$archive" -C "$extract_dir"
            extracted_bin=$(find "$extract_dir" -type f -name 'rakaly')
            ;;
        *)
            echo "Unrecognized archive format: $asset" >&2
            exit 1
            ;;
    esac

    if [ -z "$extracted_bin" ]; then
        echo "Could not find extracted rakaly binary for $platform" >&2
        exit 1
    fi

    dest="$RAKALY_DIR/$output_name"
    cp "$extracted_bin" "$dest"
    chmod +x "$dest"
    echo "Updated $dest"
done

