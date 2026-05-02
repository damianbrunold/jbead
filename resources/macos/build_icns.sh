#!/usr/bin/env bash
# Regenerates resources/macos/jbead.icns from resources/jbead.svg.
# Run on macOS; uses sips + Swift + iconutil from the base system.
#
# sips rasterizes the SVG at each iconset size; a Swift helper then
# clips the bitmap to a continuous-rounded-rect (macOS "squircle") that
# fills the canvas, and iconutil packs the iconset. Baking the squircle
# in keeps the dock icon shape consistent whether the app is running or
# pinned-but-quit (Tahoe's icon system auto-rounds the latter, so a
# square source produces a visible shape mismatch).
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
RAW="$WORK/raw"
SET="$WORK/jbead.iconset"
mkdir -p "$RAW" "$SET"

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
    sips -s format png -z "$size" "$size" "$SVG" --out "$RAW/$name" >/dev/null
done

swift - "$RAW" "$SET" <<'SWIFT_EOF'
import Foundation
import ImageIO
import CoreGraphics
import UniformTypeIdentifiers
import Darwin

guard CommandLine.arguments.count >= 3 else {
    fputs("usage: <raw-dir> <iconset-dir>\n", stderr); exit(2)
}
let rawDir = CommandLine.arguments[1]
let outDir = CommandLine.arguments[2]

let fm = FileManager.default
guard let names = try? fm.contentsOfDirectory(atPath: rawDir) else {
    fputs("error: cannot read \(rawDir)\n", stderr); exit(1)
}

for name in names where name.hasSuffix(".png") {
    let inURL = URL(fileURLWithPath: rawDir).appendingPathComponent(name)
    guard let src = CGImageSourceCreateWithURL(inURL as CFURL, nil),
          let img = CGImageSourceCreateImageAtIndex(src, 0, nil) else {
        fputs("error: cannot read \(name)\n", stderr); exit(1)
    }
    let size = img.width
    precondition(img.height == size, "expected square PNG")

    let cs = CGColorSpaceCreateDeviceRGB()
    guard let ctx = CGContext(
        data: nil, width: size, height: size, bitsPerComponent: 8,
        bytesPerRow: 0, space: cs,
        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    ) else { fputs("error: cannot create context\n", stderr); exit(1) }

    // ~22.5% of side matches the iOS/macOS squircle approximation for an
    // edge-to-edge tile. Higher = more rounded; 0 = no clip.
    let radius = CGFloat(size) * 0.225
    let rect = CGRect(x: 0, y: 0, width: size, height: size)
    ctx.addPath(CGPath(roundedRect: rect,
                       cornerWidth: radius, cornerHeight: radius,
                       transform: nil))
    ctx.clip()
    ctx.draw(img, in: rect)

    guard let masked = ctx.makeImage() else { exit(1) }
    let outURL = URL(fileURLWithPath: outDir).appendingPathComponent(name) as CFURL
    guard let dest = CGImageDestinationCreateWithURL(
            outURL, UTType.png.identifier as CFString, 1, nil) else { exit(1) }
    CGImageDestinationAddImage(dest, masked, nil)
    if !CGImageDestinationFinalize(dest) {
        fputs("error: cannot write \(name)\n", stderr); exit(1)
    }
}
SWIFT_EOF

iconutil -c icns "$SET" -o "$OUT"
echo "wrote $OUT"
