# JBead — cross-platform packaging

Three scripts, one per platform. Each uses the regular CMake install
rules declared at the top of `CMakeLists.txt` (bundle target + desktop
entry + icon + LICENSE) and then wraps the installed tree into the
native distributable.

Outputs land in `dist/` under the repository root.

## Linux — AppImage

```
packaging/linux/build_appimage.sh
```

Dependencies (Debian / Ubuntu):

```
sudo apt install qt6-base-dev qt6-base-dev-tools \
                 qt6-tools-dev qt6-tools-dev-tools \
                 cmake ninja-build
```

`linuxdeploy` and its Qt plugin (continuous releases):

```
curl -L https://github.com/linuxdeploy/linuxdeploy/releases/download/continuous/linuxdeploy-x86_64.AppImage         -o linuxdeploy-x86_64.AppImage
curl -L https://github.com/linuxdeploy/linuxdeploy-plugin-qt/releases/download/continuous/linuxdeploy-plugin-qt-x86_64.AppImage -o linuxdeploy-plugin-qt-x86_64.AppImage
chmod +x linuxdeploy-*.AppImage
export PATH="$PWD:$PATH"
```

## macOS — DMG

```
packaging/macos/build_dmg.sh                 # unsigned
packaging/macos/build_dmg.sh --sign "Developer ID Application: ..."
```

Dependencies:

```
brew install qt cmake ninja create-dmg
export PATH="$(brew --prefix qt)/bin:$PATH"
```

## Windows — NSIS installer

From a Qt x64 Developer Command Prompt:

```
powershell -File packaging\windows\build_installer.ps1
```

Requires Qt 6.5+, CMake, Ninja, NSIS (`makensis` on PATH).
