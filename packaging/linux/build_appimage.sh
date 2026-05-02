#!/usr/bin/env bash
# Builds a relocatable JBead AppImage via linuxdeploy +
# linuxdeploy-plugin-qt. Requires both binaries on $PATH and
# a Qt 6.5+ development environment.
#
# Usage:  packaging/linux/build_appimage.sh
# Output: dist/JBead-<version>-x86_64.AppImage
#
# Prerequisites (Debian/Ubuntu):
#     sudo apt install qt6-base-dev qt6-base-dev-tools qt6-tools-dev \
#                      qt6-tools-dev-tools cmake ninja-build
#     wget https://github.com/linuxdeploy/linuxdeploy/releases/download/continuous/linuxdeploy-x86_64.AppImage
#     wget https://github.com/linuxdeploy/linuxdeploy-plugin-qt/releases/download/continuous/linuxdeploy-plugin-qt-x86_64.AppImage
#     chmod +x linuxdeploy-*.AppImage
#     export PATH="$PWD:$PATH"

set -euo pipefail

SOURCE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
BUILD_DIR="$SOURCE_DIR/build-appimage"
DIST_DIR="$SOURCE_DIR/dist"
APPDIR="$BUILD_DIR/AppDir"

mkdir -p "$BUILD_DIR" "$DIST_DIR"

echo "==> configure"
cmake -S "$SOURCE_DIR" -B "$BUILD_DIR" -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DJBEAD_BUILD_TESTS=OFF

echo "==> build"
cmake --build "$BUILD_DIR"

echo "==> install into AppDir"
rm -rf "$APPDIR"
DESTDIR="$APPDIR" cmake --install "$BUILD_DIR" --prefix /usr

echo "==> linuxdeploy + plugin-qt"
QMAKE="${QMAKE:-$(command -v qmake6 || command -v qmake)}"
if [[ -z "$QMAKE" ]]; then
    echo "qmake/qmake6 not on PATH; install qt6-base-dev-tools." >&2
    exit 1
fi
export QMAKE

APPVER="$(sed -n 's/^[[:space:]]*VERSION[[:space:]]\+\([0-9.]\+\).*/\1/p' "$SOURCE_DIR/CMakeLists.txt" | head -1)"
export VERSION="$APPVER"

linuxdeploy --appdir "$APPDIR" \
            --desktop-file "$APPDIR/usr/share/applications/jbead.desktop" \
            --icon-file "$APPDIR/usr/share/icons/hicolor/32x32/apps/jbead.png" \
            --plugin qt \
            --output appimage

mv -v JBead*.AppImage "$DIST_DIR/" 2>/dev/null || mv -v jbead*.AppImage "$DIST_DIR/"

echo "==> done. AppImage in $DIST_DIR/"
ls -la "$DIST_DIR"/*.AppImage 2>/dev/null || true
