#!/usr/bin/env bash
# Builds JBead.app on macOS and wraps it into a .dmg via
# macdeployqt + hdiutil. Runs unchanged on Apple Silicon and Intel.
#
# Usage:     packaging/macos/build_dmg.sh [--sign IDENTITY]
# Output:    dist/JBead-<version>-<arch>.dmg
#
# Prerequisites:
#     brew install qt cmake ninja create-dmg
#     export PATH="$(brew --prefix qt)/bin:$PATH"

set -euo pipefail

SIGN_IDENT=""
for arg in "$@"; do
    case "$arg" in
        --sign)      shift; SIGN_IDENT="${1:?--sign requires an identity}"; shift ;;
        --sign=*)    SIGN_IDENT="${arg#--sign=}" ;;
        -h|--help)
            sed -n '2,12p' "$0"
            exit 0
            ;;
        *) echo "unknown arg: $arg" >&2; exit 2 ;;
    esac
done

SOURCE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
BUILD_DIR="$SOURCE_DIR/build-macos"
STAGE_DIR="$SOURCE_DIR/dist/macos-stage"
DIST_DIR="$SOURCE_DIR/dist"
ARCH="$(uname -m)"

mkdir -p "$BUILD_DIR" "$DIST_DIR"
rm -rf "$STAGE_DIR"

echo "==> configure"
cmake -S "$SOURCE_DIR" -B "$BUILD_DIR" -G Ninja \
      -DCMAKE_BUILD_TYPE=Release \
      -DJBEAD_BUILD_TESTS=OFF

echo "==> build"
cmake --build "$BUILD_DIR"

echo "==> install JBead.app into stage dir"
mkdir -p "$STAGE_DIR"
cmake --install "$BUILD_DIR" --prefix "$STAGE_DIR"

APP_BUNDLE="$STAGE_DIR/jbead.app"
if [[ -d "$APP_BUNDLE" ]]; then
    mv "$APP_BUNDLE" "$STAGE_DIR/JBead.app"
    APP_BUNDLE="$STAGE_DIR/JBead.app"
fi
if [[ ! -d "$APP_BUNDLE" ]]; then
    echo "error: no .app bundle was installed into $STAGE_DIR" >&2
    exit 1
fi

echo "==> macdeployqt"
macdeployqt "$APP_BUNDLE" -always-overwrite

for f in LICENSE.txt README.txt; do
    if [[ -f "$SOURCE_DIR/$f" ]]; then
        cp "$SOURCE_DIR/$f" "$STAGE_DIR/$f"
    fi
done

if [[ -n "$SIGN_IDENT" ]]; then
    echo "==> codesign"
    codesign --deep --force --options runtime --sign "$SIGN_IDENT" "$APP_BUNDLE"
fi

VERSION="$(awk -F'"' '/VERSION 0\.[0-9]/ { print $2; exit }' "$SOURCE_DIR/CMakeLists.txt" 2>/dev/null \
           || awk '/project\(jbead/,/LANGUAGES/' "$SOURCE_DIR/CMakeLists.txt" | awk '/VERSION/ { print $2; exit }')"
VERSION="${VERSION:-0.1.0}"
DMG_NAME="JBead-${VERSION}-${ARCH}.dmg"
DMG_OUT="$DIST_DIR/$DMG_NAME"
rm -f "$DMG_OUT"

echo "==> create .dmg ($DMG_NAME)"
if command -v create-dmg >/dev/null 2>&1; then
    create-dmg \
        --volname "JBead $VERSION" \
        --window-size 520 380 \
        --icon-size 96 \
        --icon "JBead.app" 140 190 \
        --app-drop-link 380 190 \
        "$DMG_OUT" "$STAGE_DIR"
else
    hdiutil create -volname "JBead $VERSION" \
        -srcfolder "$STAGE_DIR" \
        -ov -format UDZO "$DMG_OUT"
fi

echo "==> done. DMG at $DMG_OUT"
