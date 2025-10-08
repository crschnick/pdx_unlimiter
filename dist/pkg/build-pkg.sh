#!/bin/bash

#Parameters
SCRIPTPATH="$(
  cd -- "$(dirname "$0")" >/dev/null 2>&1
  pwd -P
)"
BUILDPATH="$SCRIPTPATH/../build/"
TARGET_DIRECTORY="$SCRIPTPATH/../build/dist/pkg_target"
VERSION=${1}
CANONICAL_VERSION=${2}
ARCH=${3}
PRODUCT="${4}"
PRODUCT_KEBAP="${5}"
PACKAGE_ID="${6}"
PRODUCT_URL_NAME="${7}"
OUT_FILE_NAME="${8}"
MACOS_ARCH_ID=${ARCH}

#Functions
go_to_dir() {
  pushd $1 >/dev/null 2>&1
}

log_info() {
  echo "[INFO]" $1
}

log_error() {
  echo "[ERROR]" $1
}

deleteInstallationDirectory() {
  log_info "Cleaning $TARGET_DIRECTORY directory."
  rm -rf "$TARGET_DIRECTORY"

  if [[ $? != 0 ]]; then
    log_error "Failed to clean $TARGET_DIRECTORY directory" $?
    exit 1
  fi
}

createInstallationDirectory() {
  if [ -d "${TARGET_DIRECTORY}" ]; then
    deleteInstallationDirectory
  fi
  mkdir -pv "$TARGET_DIRECTORY"

  if [[ $? != 0 ]]; then
    log_error "Failed to create $TARGET_DIRECTORY directory" $?
    exit 1
  fi
}

copyDarwinDirectory() {
  createInstallationDirectory
  cp -r "$SCRIPTPATH/scripts" "${TARGET_DIRECTORY}/"
  cp -r "$SCRIPTPATH/resources" "${TARGET_DIRECTORY}/"
  chmod -R 755 "${TARGET_DIRECTORY}/scripts"
  chmod -R 755 "${TARGET_DIRECTORY}/resources"
  chmod 755 "${TARGET_DIRECTORY}/resources/Distribution"
}

copyBuildDirectory() {
  sed -i '' -e "s/__PRODUCT__/${PRODUCT}/g" "${TARGET_DIRECTORY}/scripts/preupgrade"
  chmod -R 755 "${TARGET_DIRECTORY}/scripts/preupgrade"

  # The postinstall script is always run, even on upgrades.
  # Probably because we uninstall everything in the preupgrade
  sed -i '' -e "s/__VERSION__/${VERSION}/g" "${TARGET_DIRECTORY}/scripts/postinstall"
  sed -i '' -e "s/__PRODUCT__/${PRODUCT}/g" "${TARGET_DIRECTORY}/scripts/postinstall"
  sed -i '' -e "s/__PRODUCT_KEBAP__/${PRODUCT_KEBAP}/g" "${TARGET_DIRECTORY}/scripts/postinstall"
  chmod -R 755 "${TARGET_DIRECTORY}/scripts/postinstall"

  sed -i '' -e 's/__VERSION__/'${VERSION}'/g' "${TARGET_DIRECTORY}/resources/Distribution"
  sed -i '' -e "s/__PRODUCT__/${PRODUCT}/g" "${TARGET_DIRECTORY}/resources/Distribution"
  sed -i '' -e "s/__ARCH__/${MACOS_ARCH_ID}/g" "${TARGET_DIRECTORY}/resources/Distribution"
  sed -i '' -e "s/__PRODUCT_URL_NAME__/${PRODUCT_URL_NAME}/g" "${TARGET_DIRECTORY}/resources/Distribution"
  chmod -R 755 "${TARGET_DIRECTORY}/resources/Distribution"

  sed -i '' -e 's/__VERSION__/'${VERSION}'/g' "${TARGET_DIRECTORY}"/resources/*.html
  sed -i '' -e "s/__PRODUCT__/${PRODUCT}/g" "${TARGET_DIRECTORY}"/resources/*.html
  chmod -R 755 "${TARGET_DIRECTORY}/resources/"

  rm -rf "${TARGET_DIRECTORY}/darwinpkg"
  mkdir -p "${TARGET_DIRECTORY}/darwinpkg"

  mkdir -p "${TARGET_DIRECTORY}/darwinpkg/${PRODUCT}.app"
  cp -a "$BUILDPATH/dist/${PRODUCT}.app/" "${TARGET_DIRECTORY}/darwinpkg/${PRODUCT}.app"
  chmod -R 755 "${TARGET_DIRECTORY}/darwinpkg/${PRODUCT}.app"

  rm -rf "${TARGET_DIRECTORY}/package"
  mkdir -p "${TARGET_DIRECTORY}/package"
  chmod -R 755 "${TARGET_DIRECTORY}/package"

  rm -rf "${TARGET_DIRECTORY}/pkg"
  mkdir -p "${TARGET_DIRECTORY}/pkg"
  chmod -R 755 "${TARGET_DIRECTORY}/pkg"
}

function buildPackage() {
  log_info "Application installer package building started.(1/3)"

  # Make bundle not relocatable. Important!
  pkgbuild --analyze --root "${TARGET_DIRECTORY}/darwinpkg" "$TMPDIR/${PRODUCT}.plist"
  # The schema changed at some point, so better do both. One will fail
  plutil -replace BundleIsRelocatable -bool NO "$TMPDIR/${PRODUCT}.plist"
  plutil -replace 0.BundleIsRelocatable -bool NO "$TMPDIR/${PRODUCT}.plist"

  params=(
    --identifier "${PACKAGE_ID}"
    --version "${CANONICAL_VERSION}"
    --scripts "${TARGET_DIRECTORY}/scripts"
    --root "${TARGET_DIRECTORY}/darwinpkg"
    --install-location "/Applications/"
    --component-plist "$TMPDIR/${PRODUCT}.plist"
  )
  if [[ -n "$MACOS_DEVELOPER_ID_INSTALLER_CERTIFICATE_NAME" ]]; then
    params+=(--sign "${MACOS_DEVELOPER_ID_INSTALLER_CERTIFICATE_NAME}")
  fi
  params+=("${TARGET_DIRECTORY}/package/${PRODUCT}.pkg")
  pkgbuild "${params[@]}"
}

function buildProduct() {
  log_info "Application installer product building started.(2/3)"

  params=(
    --distribution "${TARGET_DIRECTORY}/resources/Distribution"
    --resources "${TARGET_DIRECTORY}/resources"
    --package-path "${TARGET_DIRECTORY}/package"
  )
  if [[ -n "$MACOS_DEVELOPER_ID_INSTALLER_CERTIFICATE_NAME" ]]; then
    params+=(--sign "${MACOS_DEVELOPER_ID_INSTALLER_CERTIFICATE_NAME}")
  fi
  params+=("${TARGET_DIRECTORY}/pkg/${OUT_FILE_NAME}")
  productbuild "${params[@]}"
}

function createInstaller() {
  log_info "Application installer generation process started.(3 Steps)"
  buildPackage
  buildProduct
  log_info "Application installer generation steps finished."
}

#Main script
log_info "Installer generating process started."

copyDarwinDirectory
copyBuildDirectory
createInstaller

log_info "Installer generating process finished"
exit 0
