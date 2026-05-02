#!/usr/bin/env bash
# Builds JBead.app on macOS and wraps it into a .dmg via
# macdeployqt + hdiutil. Runs unchanged on Apple Silicon and Intel.
#
# Usage:     packaging/macos/build_dmg.sh [--sign IDENTITY]
#                                         [--notarize PROFILE]
# Output:    dist/JBead-<version>-<arch>.dmg
#
# --sign:     full common name of a Developer ID Application certificate
#             in the login keychain, e.g.
#             "Developer ID Application: Jane Doe (TEAMID1234)".
#             `security find-identity -v -p codesigning` lists candidates.
# --notarize: name of a keychain profile previously stored with
#             `xcrun notarytool store-credentials`. Requires --sign.
#
# Prerequisites:
#     brew install qt cmake ninja create-dmg
#     export PATH="$(brew --prefix qt)/bin:$PATH"

set -euo pipefail

SIGN_IDENT=""
NOTARY_PROFILE=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --sign)           SIGN_IDENT="${2:?--sign requires an identity}"; shift 2 ;;
        --sign=*)         SIGN_IDENT="${1#--sign=}"; shift ;;
        --notarize)       NOTARY_PROFILE="${2:?--notarize requires a keychain profile name}"; shift 2 ;;
        --notarize=*)     NOTARY_PROFILE="${1#--notarize=}"; shift ;;
        -h|--help)
            sed -n '2,16p' "$0"
            exit 0
            ;;
        *) echo "unknown arg: $1" >&2; exit 2 ;;
    esac
done

if [[ -n "$NOTARY_PROFILE" && -z "$SIGN_IDENT" ]]; then
    echo "error: --notarize requires --sign IDENTITY" >&2
    exit 2
fi

SOURCE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
BUILD_DIR="$SOURCE_DIR/build-macos"
STAGE_DIR="$SOURCE_DIR/dist/macos-stage"
DIST_DIR="$SOURCE_DIR/dist"
ARCH="$(uname -m)"

mkdir -p "$BUILD_DIR" "$DIST_DIR"
rm -rf "$STAGE_DIR"
rm -f "$DIST_DIR"/JBead-*.dmg

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

# Strip plugins whose parent framework isn't installed by Homebrew Qt
# (qtpdf, qtvirtualkeyboard live in separate formulae). macdeployqt
# deploys them anyway and emits unresolvable-rpath errors during the
# step above; the app never loads them, so they're dead weight.
# - libqpdf.dylib is the QPdf-based image-format reader; we produce
#   PDF via QPrinter::PdfFormat (QtPrintSupport), which doesn't
#   depend on QtPdf.
# - qtvirtualkeyboard is the on-screen IME for touch devices.
rm -f "$APP_BUNDLE/Contents/PlugIns/imageformats/libqpdf.dylib"
rm -rf "$APP_BUNDLE/Contents/PlugIns/platforminputcontexts"

for f in LICENSE.txt README.txt; do
    if [[ -f "$SOURCE_DIR/$f" ]]; then
        cp "$SOURCE_DIR/$f" "$STAGE_DIR/$f"
    fi
done

if [[ -n "$SIGN_IDENT" ]]; then
    echo "==> codesign bundle (inside-out, hardened runtime, secure timestamp)"
    # Sign every nested Mach-O before sealing the bundle. Library
    # validation under the hardened runtime accepts plugins/frameworks
    # only when they share the team-id of the loading process, so
    # everything inside has to carry our signature too. --deep is
    # deprecated; explicit inside-out signing is what Apple recommends.
    codesign_args=(--force --timestamp --options runtime
                   --sign "$SIGN_IDENT")

    # 1. Loose dylibs and bundles inside Frameworks/PlugIns.
    while IFS= read -r -d '' f; do
        codesign "${codesign_args[@]}" "$f"
    done < <(find "$APP_BUNDLE/Contents" \
                  \( -name "*.dylib" -o -name "*.so" \) \
                  -type f -print0)

    # 2. Frameworks: sign each as a unit (signs the inner versioned
    #    binary via the bundle wrapper). Deepest first.
    if [[ -d "$APP_BUNDLE/Contents/Frameworks" ]]; then
        while IFS= read -r -d '' f; do
            codesign "${codesign_args[@]}" "$f"
        done < <(find "$APP_BUNDLE/Contents/Frameworks" \
                      -name "*.framework" -type d -depth -print0)
    fi

    # 3. The main bundle itself (signs Contents/MacOS/jbead too).
    codesign "${codesign_args[@]}" "$APP_BUNDLE"

    echo "==> verify codesign"
    codesign --verify --deep --strict --verbose=2 "$APP_BUNDLE"
fi

VERSION="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleShortVersionString' \
              "$APP_BUNDLE/Contents/Info.plist" 2>/dev/null || true)"
if [[ -z "$VERSION" ]]; then
    echo "error: could not read CFBundleShortVersionString from $APP_BUNDLE" >&2
    exit 1
fi
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

if [[ -n "$SIGN_IDENT" ]]; then
    echo "==> codesign DMG"
    codesign --force --timestamp --sign "$SIGN_IDENT" "$DMG_OUT"
fi

if [[ -n "$NOTARY_PROFILE" ]]; then
    echo "==> notarize (this can take a few minutes)"
    notarize_exit=0
    submission_output="$(xcrun notarytool submit "$DMG_OUT" \
                              --keychain-profile "$NOTARY_PROFILE" \
                              --wait 2>&1)" || notarize_exit=$?
    echo "$submission_output"
    if [[ $notarize_exit -ne 0 ]] || ! grep -q "status: Accepted" <<<"$submission_output"; then
        submission_id="$(grep -E '^[[:space:]]*id: ' <<<"$submission_output" \
                         | head -1 | awk '{print $2}')"
        if [[ -n "$submission_id" ]]; then
            echo "==> notarization log for $submission_id:" >&2
            xcrun notarytool log "$submission_id" \
                  --keychain-profile "$NOTARY_PROFILE" >&2 || true
        fi
        echo "==> notarization failed" >&2
        exit 1
    fi

    echo "==> staple ticket"
    xcrun stapler staple "$DMG_OUT"

    echo "==> Gatekeeper assessment"
    spctl --assess --type open \
          --context context:primary-signature --verbose "$DMG_OUT"
fi

echo "==> done. DMG at $DMG_OUT"
