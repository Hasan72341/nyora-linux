# Nyora for Linux

An AI-powered manga reader — the Linux desktop build of [Nyora](https://nyora.app), built with Compose Desktop over the shared Kotatsu engine. Ships with a bundled Java runtime, so there's nothing else to install.

## Features

- **Huge source catalogue** — browse, search and filter hundreds of online manga/manhwa/manhua sources via the shared Kotatsu parser engine.
- **Standard & Webtoon reader** — paged (LTR/RTL) and vertical webtoon modes, zoom, double-page spreads and per-title settings.
- **AI page translation** — translate a whole page at once: **Tesseract** OCR detects the text, which is then translated and typeset back over the art.
- **Dynamic colour correction** — adjust brightness, contrast and colour filters live while reading.
- **Library that stays organized** — favourites in custom categories, reading history, resume-where-you-left-off, and incognito mode.
- **Offline downloads** — download chapters for offline reading.
- **Tracker integration** — sync reading progress with online trackers.
- **Backup & restore** — export/import your library.
- **Cloud sync** — sign in with Google (loopback OAuth) and your library, favourites, categories, history and progress sync across all your Nyora devices (Supabase backend).
- **Themes** — light / dark / system.

## Install

**Quick install** (auto-detects your distro and CPU architecture):

```bash
curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh | bash
```

**Or grab a package from the [latest release](https://github.com/Hasan72341/nyora-linux/releases/latest):**

| Distro family | Package | Architectures |
|---|---|---|
| Debian · Ubuntu · Mint · Pop!_OS | `Nyora-linux-<arch>.deb` | x86_64, arm64 |
| Fedora · RHEL · openSUSE | `Nyora-linux-<arch>.rpm` | x86_64, arm64 |
| Any other distro (portable) | `Nyora-linux-<arch>-portable.tar.gz` | x86_64, arm64 |

For the portable build, extract it and run `bin/Nyora`:

```bash
tar -xzf Nyora-linux-x86_64-portable.tar.gz
./nyora/bin/Nyora
```

> 32-bit x86 isn't supported — Compose Desktop and JDK 17 are 64-bit only.

## Build from source

Requires JDK 17+ (with `jpackage`) and the `nyora-shared` submodule.

```bash
git clone --recurse-submodules https://github.com/Hasan72341/nyora-linux.git
cd nyora-linux
./gradlew :desktopApp:packageReleaseDeb   # or packageReleaseRpm / createReleaseDistributable
```

Packages land in `desktopApp/build/compose/binaries/main-release/`.

## Releasing

Pushing a `v*` tag triggers [`.github/workflows/build-linux.yml`](.github/workflows/build-linux.yml), which builds the `.deb`, `.rpm`, and portable `.tar.gz` for x86_64 and arm64 and publishes them to a GitHub Release.
