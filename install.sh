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

elif command -v dnf >/dev/null 2>&1 || command -v rpm >/dev/null 2>&1 || command -v zypper >/dev/null 2>&1; then
  say "Fedora/RHEL/openSUSE family detected — installing the .rpm"
  fetch "${BASE}/Nyora-linux-${ARCH}.rpm" "$TMP/nyora.rpm"
  if command -v dnf >/dev/null 2>&1; then
    $SUDO dnf install -y "$TMP/nyora.rpm"
  elif command -v zypper >/dev/null 2>&1; then
    $SUDO zypper --non-interactive install --allow-unsigned-rpm "$TMP/nyora.rpm"
  else
    $SUDO rpm -i --force "$TMP/nyora.rpm"
  fi
  say "Installed. Launch Nyora from your app menu or run: nyora"

else
  say "No supported package manager found — installing the portable build to ~/.local"
  fetch "${BASE}/Nyora-linux-${ARCH}-portable.tar.gz" "$TMP/nyora.tgz"
  DEST="$HOME/.local/opt/nyora"
  mkdir -p "$DEST"
  tar -xzf "$TMP/nyora.tgz" -C "$DEST" --strip-components=1
  mkdir -p "$HOME/.local/bin"
  BIN="$(find "$DEST/bin" -maxdepth 1 -type f | head -1)"
  [ -n "$BIN" ] || die "Portable archive did not contain a launcher."
  ln -sf "$BIN" "$HOME/.local/bin/nyora"
  say "Installed to $DEST"
  case ":$PATH:" in
    *":$HOME/.local/bin:"*) say "Run: nyora" ;;
    *) say "Run: ~/.local/bin/nyora  (add ~/.local/bin to your PATH to use 'nyora' directly)" ;;
  esac
fi
