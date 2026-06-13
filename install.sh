#!/usr/bin/env bash
# Nyora for Linux — one-line installer.
#
#   curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh | bash
#
# Detects your CPU architecture and distro family, downloads the matching
# package from the latest GitHub Release, and installs it. Falls back to a
# portable extract under ~/.local when no system package manager fits.
set -euo pipefail

REPO="Hasan72341/nyora-linux"
BASE="https://github.com/${REPO}/releases/latest/download"

say()  { printf '\033[1;36m›\033[0m %s\n' "$*"; }
err()  { printf '\033[1;31m✗\033[0m %s\n' "$*" >&2; }
die()  { err "$*"; exit 1; }

# ── Architecture ──────────────────────────────────────────────────────────────
case "$(uname -m)" in
  x86_64|amd64)        ARCH="x86_64" ;;
  aarch64|arm64)       ARCH="arm64" ;;
  *) die "Unsupported CPU architecture: $(uname -m). Nyora ships x86_64 and arm64 only." ;;
esac
say "Detected architecture: ${ARCH}"

SUDO=""
if [ "$(id -u)" -ne 0 ]; then
  command -v sudo >/dev/null 2>&1 && SUDO="sudo"
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
fetch() { curl -fL --progress-bar "$1" -o "$2"; }

# ── Pick the install method that fits this distro ─────────────────────────────
if command -v apt-get >/dev/null 2>&1 || command -v dpkg >/dev/null 2>&1; then
  say "Debian/Ubuntu family detected — installing the .deb"
  fetch "${BASE}/Nyora-linux-${ARCH}.deb" "$TMP/nyora.deb"
  if command -v apt-get >/dev/null 2>&1; then
    $SUDO apt-get update -qq || true
    $SUDO apt-get install -y "$TMP/nyora.deb"
  else
    $SUDO dpkg -i "$TMP/nyora.deb" || $SUDO apt-get -f install -y
  fi
  say "Installed. Launch Nyora from your app menu or run: nyora"

# Trigger the RPM path on a real RPM package manager (dnf/yum/zypper), NOT a bare
# `rpm` binary — Arch can have `rpm` installed for building and must not be treated
# as an RPM system (it falls through to the portable build below).
elif command -v dnf >/dev/null 2>&1 || command -v yum >/dev/null 2>&1 || command -v zypper >/dev/null 2>&1; then
  say "Fedora/RHEL/Rocky/openSUSE family detected — installing the .rpm"
  fetch "${BASE}/Nyora-linux-${ARCH}.rpm" "$TMP/nyora.rpm"
  if command -v dnf >/dev/null 2>&1; then
    $SUDO dnf install -y "$TMP/nyora.rpm"
  elif command -v zypper >/dev/null 2>&1; then
    $SUDO zypper --non-interactive install --allow-unsigned-rpm "$TMP/nyora.rpm"
  elif command -v yum >/dev/null 2>&1; then
    $SUDO yum install -y "$TMP/nyora.rpm"
  else
    $SUDO rpm -i --force "$TMP/nyora.rpm"
  fi
  say "Installed. Launch Nyora from your app menu or run: nyora"

else
  # Arch / Manjaro (pacman) and any other distro — the portable build is glibc-based
  # and runs anywhere, with its own bundled Java runtime.
  say "Using the portable build (works on Arch, Manjaro & any other glibc distro) → ~/.local"
  fetch "${BASE}/Nyora-linux-${ARCH}-portable.tar.gz" "$TMP/nyora.tgz"
  DEST="$HOME/.local/opt/nyora"
  mkdir -p "$DEST"
  tar -xzf "$TMP/nyora.tgz" -C "$DEST" --strip-components=1
  mkdir -p "$HOME/.local/bin"
  BIN="$(find "$DEST/bin" -maxdepth 1 -type f | head -1)"
  [ -n "$BIN" ] || die "Portable archive did not contain a launcher."
  ln -sf "$BIN" "$HOME/.local/bin/nyora"

  # Add an application-menu entry so Nyora shows in the launcher (the app also
  # self-registers on first run; this makes it appear immediately).
  APPS="$HOME/.local/share/applications"; mkdir -p "$APPS"
  ICON="$(find "$DEST" -name '*.png' 2>/dev/null | head -1)"
  cat > "$APPS/nyora.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=Nyora
GenericName=Manga Reader
Comment=AI-powered manga reader
Exec="$BIN" %U
Icon=${ICON:-nyora}
Terminal=false
Categories=Graphics;Utility;Viewer;
EOF
  update-desktop-database "$APPS" >/dev/null 2>&1 || true
  say "Installed to $DEST · added to your application menu"
  case ":$PATH:" in
    *":$HOME/.local/bin:"*) say "Run: nyora" ;;
    *) say "Run: ~/.local/bin/nyora  (add ~/.local/bin to your PATH to use 'nyora' directly)" ;;
  esac
fi
