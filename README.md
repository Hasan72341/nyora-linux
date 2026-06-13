<div align="center">

<img src="https://nyora.pages.dev/icon.png" width="112" alt="Nyora"/>

# Nyora — Linux

### Read like the world can wait.

A native **Linux** manga reader built from scratch with **Compose Multiplatform** — hundreds of online sources, AI page translation, and a one-line installer that works on every major distro. Bundled Java runtime, nothing else to install.

[![License: Apache 2.0](https://img.shields.io/github/license/Hasan72341/nyora-linux?color=blue)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/Hasan72341/nyora-linux?label=download&color=0ae448)](https://github.com/Hasan72341/nyora-linux/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Hasan72341/nyora-linux/total?color=9d95ff)](https://github.com/Hasan72341/nyora-linux/releases)
[![Stars](https://img.shields.io/github/stars/Hasan72341/nyora-linux?style=social)](https://github.com/Hasan72341/nyora-linux/stargazers)

**[⬇️ Releases](https://github.com/Hasan72341/nyora-linux/releases/latest)** · **[🌐 nyora.pages.dev](https://nyora.pages.dev)**

</div>

---

## ⬇️ Install

**One line — auto-detects your distro & CPU:**

```bash
curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh | bash
```

Covers **Debian/Ubuntu/Kali/Parrot** (`.deb`), **Fedora/RHEL/Rocky/openSUSE** (`.rpm`), and **Arch/Manjaro + anything else** (portable, glibc) — for **x86_64 and ARM64**. Nyora adds itself to your application menu automatically. Or grab a package from the **[Releases page](https://github.com/Hasan72341/nyora-linux/releases/latest)**.

## ✨ Features

- 📚 **Hundreds of online sources** — manga, manhwa & manhua.
- 🌐 **AI page translation** — **Tesseract** OCR detects the text, then it's translated and typeset back over the page.
- 📖 **Standard & Webtoon reader** — LTR / RTL / vertical, zoom, double-page, per-title settings.
- 🎨 **Dynamic colour correction** while reading.
- 🗂️ Favourites in custom categories, history, resume, **incognito**, offline downloads, backup/restore.
- 🔄 **Tracker integration** + ☁️ **cloud sync** (sign in with Google; library & progress sync across devices).
- 🖥️ Auto app-menu entry · responsive window (fits any resolution) · light / dark / system themes.

## 🛠️ Build from source

Requires **JDK 17+** (with `jpackage`) and the `nyora-shared` submodule.

```bash
git clone --recurse-submodules https://github.com/Hasan72341/nyora-linux.git
cd nyora-linux
./gradlew :desktopApp:run                       # run
./gradlew :desktopApp:packageReleaseDeb          # .deb  (or packageReleaseRpm / createReleaseDistributable)
```

## 🧩 Nyora on every platform

| Platform | Repo | Get it |
|---|---|---|
| 🐧 Linux | **nyora-linux** *(you are here)* | [deb · rpm · curl](https://github.com/Hasan72341/nyora-linux/releases/latest) |
| 🤖 Android | [nyora-android](https://github.com/Hasan72341/nyora-android) | [APK](https://github.com/Hasan72341/nyora-android/releases/latest) |
| 🪟 Windows | [nyora-windows](https://github.com/Hasan72341/nyora-windows) | [.exe (x64/ARM64)](https://github.com/Hasan72341/nyora-windows/releases/latest) |
| 🍎 macOS | [nyora-mac](https://github.com/Hasan72341/nyora-mac) | [.dmg / `brew`](https://github.com/Hasan72341/nyora-mac/releases/latest) |
| 📱 iOS / iPadOS | [nyora-ios](https://github.com/Hasan72341/nyora-ios) | [sideload IPA](https://github.com/Hasan72341/nyora-ios/releases/latest) |
| 🌍 Web | — | [nyoraweb.pages.dev](https://nyoraweb.pages.dev) |

## 🏗️ Tech

Kotlin · **Compose Multiplatform for Desktop** · a shared Kotlin engine (`nyora-shared`) running the source parsers + a loopback REST API · jpackage `.deb`/`.rpm`/portable with a bundled JRE.

## 🤝 Contributing

Issues & PRs welcome. ⭐ **Star the repo** if you like Nyora!

## 📄 License

Licensed under the **Apache License 2.0** (see [`LICENSE`](LICENSE)). Original code, built from scratch — source-compatible with Tachiyomi/Kotatsu-style sources but not a fork.

## 🙏 Credits

Developed & maintained by **Md Hasan Raza** — [GitHub](https://github.com/Hasan72341) · [Instagram](https://instagram.com/md_hasan_raza____) · [LinkedIn](https://www.linkedin.com/in/md-hasan-raza) · hasanraza96@outlook.com

> Nyora is not affiliated with any of the manga sources it can access.
