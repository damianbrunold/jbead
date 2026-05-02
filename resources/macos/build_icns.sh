#!/usr/bin/env bash
# Regenerates resources/macos/jbead.icns from resources/jbead.svg.
# Run on macOS; uses sips + iconutil from the base system.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
SVG="$HERE/../jbead.svg"
OUT="$HERE/jbead.icns"

if [[ ! -f "$SVG" ]]; then
    echo "error: $SVG not found" >&2
    exit 1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
SET="$WORK/jbead.iconset"
mkdir -p "$SET"

# (filename, pixel size) pairs covering the standard macOS icon set.
sizes=(
    "icon_16x16.png 16"
    "icon_16x16@2x.png 32"
    "icon_32x32.png 32"
    "icon_32x32@2x.png 64"
    "icon_128x128.png 128"
    "icon_128x128@2x.png 256"
    "icon_256x256.png 256"
    "icon_256x256@2x.png 512"
    "icon_512x512.png 512"
    "icon_512x512@2x.png 1024"
)

for entry in "${sizes[@]}"; do
    name="${entry% *}"
    size="${entry##* }"
    sips -s format png -z "$size" "$size" "$SVG" --out "$SET/$name" >/dev/null
done

iconutil -c icns "$SET" -o "$OUT"
echo "wrote $OUT"
