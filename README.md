<div align="center">

<img src="https://nyora.xyz/icon.png" width="120" alt="Nyora"/>

# Nyora — Linux

### Read like the world can wait.

A fast, free, ad-free, open-source manga reader for Linux, built from scratch with Compose Multiplatform. Hundreds of online sources, whole-page AI translation, offline downloads, and free cloud sync across every device — installed with one line on every major distro, with a Java runtime bundled so there is nothing else to set up.

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black" alt="Linux"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
</p>

[![License: Apache 2.0](https://img.shields.io/github/license/Hasan72341/nyora-linux?color=blue)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/Hasan72341/nyora-linux?label=download&color=0ae448)](https://github.com/Nyora-Manga/nyora-linux/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Hasan72341/nyora-linux/total?color=9d95ff)](https://github.com/Nyora-Manga/nyora-linux/releases)
[![Stars](https://img.shields.io/github/stars/Hasan72341/nyora-linux?style=social)](https://github.com/Nyora-Manga/nyora-linux/stargazers)
[![PRs welcome](https://img.shields.io/badge/PRs-welcome-0ae448?style=flat-square)](https://github.com/Nyora-Manga/nyora-linux/issues)

[![Download deb · rpm](https://img.shields.io/badge/Download-deb_·_rpm-FCC624?style=for-the-badge&logo=linux&logoColor=black)](https://github.com/Nyora-Manga/nyora-linux/releases/latest)
[![Website](https://img.shields.io/badge/Website-nyora.xyz-FF4655?style=for-the-badge&logo=githubpages&logoColor=white)](https://nyora.xyz)
[![Open Web App](https://img.shields.io/badge/Open-Web_App-5A0FC8?style=for-the-badge&logo=pwa&logoColor=white)](https://web.nyora.xyz)
[![Catalogue](https://img.shields.io/badge/Source_Catalogue-JSON-FF6C37?style=for-the-badge&logo=json&logoColor=white)](https://raw.githubusercontent.com/Nyora-Manga/nyora-data-driven/main/catalogue.json)

</div>

---

> **Install in one line — no JDK, no dependencies, no account.**
>
> ```bash
> curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh | bash
> ```
>
> The installer auto-detects your distro and CPU, drops in the right `.deb`/`.rpm`/portable build with a Java runtime already inside, and adds Nyora to your application menu. Open-source and auditable — no ads, no tracking, and you never need to sign in to read.

## Why you'll love it

- **One line and you're reading.** Paste the installer, and Nyora detects your distribution and architecture, installs the matching native package, and adds itself to your app menu. There is no JDK to set up — every build ships its own runtime.
- **Read anything, in your language.** Whole-page AI translation reads the text printed on a page and typesets the translation back over the original art, so you can read titles no group has ever translated — without leaving the panel.
- **Your library is yours.** No account needed to read. Cloud sync is optional and free; skip it and Nyora stays a fully local, offline-capable reader. Nothing leaves your machine unless you opt in.
- **Nothing to distrust.** No ads, no analytics SDKs, no tracking. The packaging and source parsers are open under Apache-2.0, so anyone can audit exactly what runs.
- **Built for Linux, not ported to it.** Native Compose desktop UI, responsive from a small laptop panel to an ultrawide, friendly to tiling window managers, with light, dark and system themes.

## Screenshots

Captured running the packaged app on Ubuntu 24.04 (GNOME/Wayland). More are in [`docs/screenshots/`](docs/screenshots), and a fuller tour lives on the [Nyora website](https://nyora.xyz).

<div align="center">

| | |
|---|---|
| ![Home](docs/screenshots/home.png)<br/>**Home** — A "welcome back" home with a Discover grid across the desktop sidebar layout. | ![Reader](docs/screenshots/reader.png)<br/>**Reader** — A full-width reader with a page-progress bar and a bottom toolbar. |
| ![Discover](docs/screenshots/discover.png)<br/>**Discover** — A featured title in a large hero banner above a row of recommended covers. | ![Library](docs/screenshots/library.png)<br/>**Library** — Your library in custom categories, sorted however you like. |
| ![Details](docs/screenshots/details.png)<br/>**Details** — Synopsis, genre tags and the full chapter list with start-reading and downloads. | ![Settings](docs/screenshots/settings.png)<br/>**Settings** — Appearance, reader, translation, cloud sync, trackers and backup. |
| ![Explore](docs/screenshots/explore.png)<br/>**Explore** — Every installed source, tagged by language, with pin and add-catalog controls. | ![Global search](docs/screenshots/search.png)<br/>**Global search** — Search across every source at once, grouped by source. |
| ![History](docs/screenshots/history.png)<br/>**History** — Recent reads with cover thumbnails, timestamps and progress. | ![Updates](docs/screenshots/updates.png)<br/>**Updates** — New chapters across your whole library in one feed. |
| ![Stats](docs/screenshots/stats.png)<br/>**Stats** — Reading streak, chapters read, favourites and your top sources. | |

</div>

## About

Nyora is a native Linux manga reader built from scratch with Compose Multiplatform for Desktop. It brings a huge catalogue of online sources, AI-powered whole-page translation, offline downloads, and free Nyora Cloud sync into one clean, responsive desktop application. It installs with a single command on every major distribution — Debian, Ubuntu, Fedora, RHEL, openSUSE, Arch and more, on both x86_64 and ARM64 — and ships its own Java runtime, so there are no dependencies to chase and nothing to configure. It is 100% free, ad-free, has no tracking, and is licensed under Apache-2.0.

## Highlights

| Pillar | What you get |
|---|---|
| Translate | Whole-page AI translation: Tesseract OCR reads each page and the translation is typeset back over the original art. |
| Download | Offline chapter downloads to disk, plus full backup and restore of your setup. |
| Sources | Hundreds of online sources for manga, manhwa and manhua, browsable from one built-in catalogue. |
| Sync | Free Nyora Cloud sync — create an account with email and password and your library follows you across Linux, Windows, macOS, Android, iOS and Web. |
| Open-Source | Free, ad-free, no tracking, no account required to read, fully auditable, licensed under Apache-2.0. |

## Table of Contents

- [Why you'll love it](#why-youll-love-it)
- [Screenshots](#screenshots)
- [About](#about)
- [Highlights](#highlights)
- [Features](#features)
  - [Translate](#translate)
  - [Download & Offline](#download--offline)
  - [Sources & Discovery](#sources--discovery)
  - [Cloud Sync](#cloud-sync)
  - [Reader](#reader)
  - [Trackers](#trackers)
  - [Privacy & Open Source](#privacy--open-source)
  - [Themes & Personalisation](#themes--personalisation)
- [Capability Matrix](#capability-matrix)
- [Installation](#installation)
- [Build from Source](#build-from-source)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Nyora on Every Platform](#nyora-on-every-platform)
- [Roadmap](#roadmap)
- [FAQ](#faq)
- [Contributing](#contributing)
  - [Ways to contribute](#ways-to-contribute)
  - [Development setup](#development-setup)
  - [Where things live](#where-things-live)
  - [Good first contributions](#good-first-contributions)
  - [The biggest help wanted: porting sources](#the-biggest-help-wanted-porting-sources)
  - [PR & issue etiquette](#pr--issue-etiquette)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## Features

### Translate

Nyora reads manga in your language without ever leaving the panel. Tesseract OCR detects the text printed on a page, that text is translated, and the result is typeset back over the original artwork. Because the translation sits over the art rather than in a separate pane, the experience stays immersive — you keep the panels, the pacing and the layout exactly as the artist intended, just with dialogue you can read. It works on whole pages, so you are not limited to pre-translated chapters or specific sources, and it reaches titles no human group has translated. Translation targets already in use include English and Hindi.

### Download & Offline

Save chapters to disk and keep reading wherever the signal drops — on a plane, a train, the subway, anywhere offline. Downloaded chapters live locally, so they open instantly and never depend on a source staying up. Beyond individual downloads, Nyora can back up and restore your entire setup — library, categories, history, bookmarks and progress — on demand, making it painless to move machines or recover after a reinstall. A restore re-creates your reading life exactly as it was, and pairs naturally with cloud sync for a belt-and-braces backup.

### Sources & Discovery

Nyora ships with a built-in catalogue of hundreds of online sources covering manga, manhwa and manhua. Browse, search and filter the whole catalogue from one clean reader interface — no extensions to install, no external lists to maintain. Discovery, search and per-source filters all live inside the app, so finding the next thing to read is a first-class part of the experience rather than an afterthought. Sources are powered by the shared `nyora-shared` engine, backed by the [`nyora-data-driven`](https://github.com/Nyora-Manga/nyora-data-driven) source definitions, keeping parser behaviour consistent with the macOS and Windows builds.

### Cloud Sync

Create a free Nyora Cloud account with your email and a password, and Nyora keeps your reading life in sync across every platform. Your library, custom categories, reading history, bookmarks and exact reading progress follow you between Linux, Windows, macOS, Android, iOS and the Web. Start a chapter on your phone during a commute and pick up at the exact page on your Linux desktop at home — no manual exports, no paid tier. Sync runs on Nyora Cloud, a self-hosted backend, and is entirely opt-in: skip the sign-in and Nyora stays a fully local, offline-capable reader.

### Reader

The reader supports both standard and webtoon modes with left-to-right, right-to-left and continuous vertical layouts. You get zoom, double-page spreads, and per-title settings, so each series can be read the way it is meant to be. The window is fully responsive and fits any resolution, from a small laptop panel to an ultrawide display, and adapts to tiling window managers. Dynamic colour correction is applied while you read for a consistent, comfortable image. An incognito mode lets you read without writing to your history when you want a session to stay private.

### Trackers

Nyora integrates with tracking services, so the lists you keep elsewhere stay in sync with what you read here. Update progress as you go and keep your external reading lists accurate without manual bookkeeping.

### Privacy & Open Source

Nyora is 100% free, ad-free, and contains no tracking. No account is required to read — a Nyora Cloud account is only ever needed if you opt into cloud sync. The project is built from scratch and the packaging and source parsers live in the open under Apache-2.0, so the build is fully auditable. There are no analytics SDKs harvesting your reading habits and no advertising network in the build.

### Themes & Personalisation

Choose light, dark or system themes, and organise your collection into custom categories of favourites. History, resume-where-you-left-off, incognito mode, offline downloads and full backup/restore are all built in. On Linux specifically, the installer adds an application-menu entry automatically, and the window adapts responsively to whatever resolution and tiling setup your desktop uses.

## Capability Matrix

What the Linux build ships today.

| Capability | Status |
|---|---|
| Whole-page AI translation (Tesseract OCR) | Yes |
| Offline chapter downloads | Yes |
| Backup & restore | Yes |
| Hundreds of built-in sources | Yes |
| Free Nyora Cloud sync (email + password) | Yes |
| Trackers | Yes |
| Standard + webtoon reader, LTR / RTL / vertical | Yes |
| Light / dark / system themes | Yes |
| Incognito mode | Yes |
| Bundled Java runtime (no JDK/JRE needed) | Yes |
| One-line installer with distro + CPU autodetect | Yes |
| Native `.deb` / `.rpm` + portable build | Yes |
| x86_64 and ARM64 | Yes |
| Account required to read | No |
| Ads / tracking | No |

## Installation

Nyora for Linux is distributed as native packages and a portable build, all with a bundled Java runtime, so there are no extra dependencies to install. It is open-source and auditable — you can read the installer script before you run it, there are no ads or tracking, and no account is needed to read.

### One-line installer (recommended)

The installer auto-detects your distribution and CPU architecture, downloads the right artifact, installs it, and adds Nyora to your application menu:

```bash
curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh | bash
```

Prefer to look before you leap? That is encouraged. Read [`install.sh`](install.sh) first — it is a short, plain Bash script — or download it and run it locally so nothing is piped straight into a shell:

```bash
curl -fsSL https://raw.githubusercontent.com/Hasan72341/nyora-linux/main/install.sh -o nyora-install.sh
less nyora-install.sh   # inspect it
bash nyora-install.sh
```

Under the hood it inspects your system (distro family via the package manager / release metadata, and CPU via `uname`), maps that to the correct artifact, and chooses native package vs. portable accordingly:

| Distro family | CPU | Artifact installed |
|---|---|---|
| Debian / Ubuntu / Kali / Parrot | x86_64 / ARM64 | `.deb` package |
| Fedora / RHEL / Rocky / openSUSE | x86_64 / ARM64 | `.rpm` package |
| Arch / Manjaro / anything else | x86_64 / ARM64 | Portable build (glibc) |

A Java runtime is bundled with every artifact, so you never install or manage a JDK/JRE yourself. The `.deb` and `.rpm` paths also register a desktop application-menu entry automatically.

### Manual package install

Prefer to inspect or pin a specific version? Download a package directly from the [Releases page](https://github.com/Nyora-Manga/nyora-linux/releases/latest) and install it with your package manager.

Debian / Ubuntu and derivatives:

```bash
sudo apt install ./nyora_*.deb
```

Fedora / RHEL / openSUSE and derivatives:

```bash
sudo rpm -i nyora-*.rpm
```

### Portable build

For Arch, Manjaro, or any distribution where you would rather not install a package, download the portable (glibc) build from Releases, extract it, and run the launcher inside. The bundled runtime means it works without system-wide changes — handy for a USB stick or a read-only environment.

### Requirements & troubleshooting

- Requirements: a modern x86_64 or ARM64 Linux distribution with glibc. No separate Java installation is required — a runtime ships inside every build.
- Not in your menu after install? The one-line installer and the `.deb` / `.rpm` packages register the application-menu entry automatically; if you used the portable build, launch it from the extracted directory.
- Wrong artifact downloaded? The installer detects distro and CPU automatically, but on an unusual setup you can always grab the exact `.deb`, `.rpm` or portable archive for your architecture from the [Releases page](https://github.com/Nyora-Manga/nyora-linux/releases/latest).
- Updating: re-run the one-line installer, or download and install the latest package from Releases.

## Build from Source

Building Nyora for Linux requires JDK 17 or newer (with `jpackage`, used to produce the native installers) and the `nyora-shared` submodule, which contains the shared Kotlin engine. That engine is open-source and public, so cloning with submodules pulls it in and a full from-scratch build — engine included — works for anyone.

```bash
git clone --recurse-submodules https://github.com/Nyora-Manga/nyora-linux.git
cd nyora-linux
./gradlew :desktopApp:run                       # run
./gradlew :desktopApp:packageReleaseDeb          # .deb  (or packageReleaseRpm / createReleaseDistributable)
```

- `:desktopApp:run` launches the app locally for development.
- `:desktopApp:packageReleaseDeb` produces a `.deb` package.
- `:desktopApp:packageReleaseRpm` produces an `.rpm` package.
- `:desktopApp:createReleaseDistributable` produces a portable distributable.

Each packaging task uses `jpackage` to bundle a JRE into the output, so the resulting artifacts run without a system Java installation. If you forgot `--recurse-submodules` when cloning, initialise the submodule with `git submodule update --init --recursive` before building.

> **Heads-up for contributors:** `nyora-shared` is a **public, open-source** repository ([github.com/Nyora-Manga/nyora-shared](https://github.com/Nyora-Manga/nyora-shared), Apache-2.0). Because it is vendored here as a git submodule, anyone can do a full from-scratch build that compiles the engine — just clone with `--recurse-submodules` (or run `git submodule update --init --recursive`) and everything builds, engine included. The entire Compose UI, packaging and Linux integration layer in this repo is open too, and the shared engine itself takes PRs like any open repo, so there is a great deal of meaningful work you can do across both. See [Contributing](#contributing) for exactly where to start.

## Tech Stack

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black" alt="Linux"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
  <img src="https://img.shields.io/badge/SQLDelight-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLDelight"/>
  <img src="https://img.shields.io/badge/OkHttp-00A98F?style=for-the-badge&logo=square&logoColor=white"/>
</p>

- **Kotlin 2.1** — the language the entire application and shared engine are written in.
- **Compose Multiplatform for Desktop** — the declarative UI toolkit powering Nyora's native Linux interface.
- **Linux** — the target platform, packaged as native `.deb` / `.rpm` and portable builds with a bundled JRE.
- **Gradle** — the build system that drives compilation and `jpackage` packaging.
- **SQLDelight** — local SQLite database in the shared engine for the manga library.
- **OkHttp 5** — HTTP client for source fetching in the shared engine.
- **Tesseract OCR** — on-device text detection for the translation pipeline.

## Architecture

Nyora for Linux is a Compose Multiplatform for Desktop application written in Kotlin. The user interface is built with Compose, while the heavy lifting of fetching and parsing content is handled by a shared Kotlin engine called `nyora-shared`. That engine runs the source parsers behind a loopback REST API: the desktop UI talks to a local server over the loopback interface, which keeps source-handling logic cleanly separated from the presentation layer and lets the same engine be reused across platforms. Distribution is handled by `jpackage`, which produces `.deb`, `.rpm` and portable builds — each with a bundled JRE, so the app is self-contained and needs no external Java installation. Translation is powered by Tesseract OCR, which detects on-page text so it can be translated and typeset back over the original artwork.

## Nyora on Every Platform

| Platform | Repo | Get it |
|---|---|---|
| Linux | nyora-linux (you are here) | [deb · rpm · curl](https://github.com/Nyora-Manga/nyora-linux/releases/latest) |
| Android | [nyora-android](https://github.com/Nyora-Manga/nyora-android) | [APK](https://github.com/Nyora-Manga/nyora-android/releases/latest) |
| macOS | [nyora-mac](https://github.com/Nyora-Manga/nyora-mac) | [.dmg / `brew`](https://github.com/Nyora-Manga/nyora-mac/releases/latest) |
| Windows | [nyora-windows](https://github.com/Nyora-Manga/nyora-windows) | [.exe (x64/ARM64)](https://github.com/Nyora-Manga/nyora-windows/releases/latest) |
| iOS / iPadOS | [nyora-ios](https://github.com/Nyora-Manga/nyora-ios) | [sideload IPA](https://github.com/Nyora-Manga/nyora-ios/releases/latest) |
| Web | [nyora-web](https://github.com/Nyora-Manga/nyora-web) | [web.nyora.xyz](https://web.nyora.xyz) |
| Data-driven engine | [`nyora-data-driven`](https://github.com/Nyora-Manga/nyora-data-driven) | 35 generic source templates — powers the source catalogue |

The Linux, macOS and Windows desktop apps share one Kotlin engine (`nyora-shared`), so source parsing, sync and downloads behave consistently across all three.

## Roadmap

Honest, already-stated direction — no dates or version promises:

- Broader distro and packaging coverage for the one-line installer.
- Continued source-parser expansion via the shared `nyora-shared` engine, kept in step with the macOS and Windows builds.
- Reader and translation refinements driven by community feedback.

Have a request? Open an [issue](https://github.com/Nyora-Manga/nyora-linux/issues).

## FAQ

**Is Nyora free?**
Yes. Nyora is 100% free and ad-free, with no tracking. Cloud sync is free too. There is no paid tier and nothing is locked behind a subscription.

**Is it safe — and why is the installer not "signed" like a store app?**
Nyora is distributed straight from GitHub Releases rather than a paid app store, so it does not carry commercial code-signing. That is expected for an open-source project of this kind, and it does not mean it is unsafe: the app, the packaging and the [`install.sh`](install.sh) script are open under Apache-2.0, so you can read exactly what runs before you run it. If you would rather not pipe the script into a shell, download it first, inspect it, then run it locally — the steps are in [Installation](#installation).

**Are there ads or tracking?**
No. There is no advertising network and no analytics harvesting your reading habits. The build is fully auditable.

**Do I need an account?**
No account is required to read. A free Nyora Cloud account (email + password) is only needed if you want to enable free cloud sync across your devices.

**Will my data stay private?**
Yes. If you never sign in, Nyora is fully local and nothing leaves your machine. If you opt into cloud sync, only your library, categories, history, bookmarks and reading progress are synced — there are no analytics SDKs and no advertising network in the build.

**Where do the sources and content come from?**
Nyora ships with a built-in catalogue of hundreds of online sources for manga, manhwa and manhua. Nyora is just a reader — it is not affiliated with any of these sources and hosts no content itself.

**Is cloud sync private, and what does it sync?**
Sync is opt-in and tied to your Nyora Cloud account (email + password). It runs on Nyora Cloud, a self-hosted backend, and syncs your library, custom categories, reading history, bookmarks and reading progress across Linux, Windows, macOS, Android, iOS and the Web. If you never sign in, Nyora stays fully local and nothing leaves your machine.

**Can I read offline?**
Yes. Download chapters to disk and read them anywhere with no connection. You can also back up and restore your entire setup, and pair that with cloud sync as a second safety net.

**How does translation work on Linux?**
Tesseract OCR detects the text on each page, that text is translated, and the result is typeset back over the original art — so you read whole pages in-place rather than in a separate pane. Translation targets already in use include English and Hindi.

**Do I need to install Java?**
No. Every artifact — `.deb`, `.rpm` and portable — bundles its own JRE via `jpackage`, so there is no JDK/JRE to install or manage.

**Which distributions and architectures are supported?**
Debian/Ubuntu/Kali/Parrot (`.deb`), Fedora/RHEL/Rocky/openSUSE (`.rpm`), and Arch/Manjaro plus anything else (portable), on both x86_64 and ARM64.

**Can I contribute to the engine?**
Yes. The shared Kotlin engine, `nyora-shared`, is a public, open-source repository ([github.com/Nyora-Manga/nyora-shared](https://github.com/Nyora-Manga/nyora-shared), Apache-2.0) and it takes PRs like any open repo — the source/parser runtime, the loopback REST server, the SQLDelight store, Nyora Cloud sync and the downloads manager are all open to contribution. It is vendored here as a git submodule, so cloning with `--recurse-submodules` gives you a full from-scratch build to work against. See [Contributing](#contributing) for where to start.

**How do I update?**
Re-run the one-line installer, or download and install the latest package from the Releases page.

## Contributing

Nyora is built and maintained in the open, and contributions of every size are genuinely welcome — whether you write Kotlin, package software, speak another language, or simply enjoy the app and want to help others find it. You do not need to be a maintainer or a desktop expert to make a real difference, and you can start today.

One thing worth knowing up front: the desktop apps depend on a shared Kotlin engine, `nyora-shared`, which is a fully **public, open-source** repository ([github.com/Nyora-Manga/nyora-shared](https://github.com/Nyora-Manga/nyora-shared), Apache-2.0). It is vendored here as a git submodule, so anyone can do a full from-scratch build that compiles the engine — just clone with `--recurse-submodules`. Contributions are welcome on both sides: the engine itself (source/parser runtime, the loopback REST server, the SQLDelight store, Nyora Cloud sync, the downloads manager) takes PRs like any open repo, and the entire Compose UI, the Linux packaging, the installer and the distro integration in this repo are open too. Wherever something lives in the shared engine rather than this repo, it is called out below.

### Ways to contribute

You can help even if you have never written a line of Kotlin:

- **Report bugs.** Hit something odd? Open an [issue](https://github.com/Nyora-Manga/nyora-linux/issues) with your distro, architecture, and the steps that triggered it. Clear reports are some of the most useful contributions there are.
- **Test releases on your distro.** Nyora targets a lot of distributions; real-world install reports — "the one-line installer worked cleanly on Pop!_OS ARM64" or "the `.rpm` failed on this version" — directly improve the installer for everyone. This is a fantastic, low-barrier way to help, and needs no code.
- **Improve packaging and distro coverage.** Better `.deb`/`.rpm` metadata, broader autodetection in [`install.sh`](install.sh), or portable-build fixes are high-impact and live entirely in this repo.
- **Polish the UI.** Spacing, theming, keyboard handling, responsiveness on tiling window managers — the Compose code is right here and open to improvement.
- **Contribute to the shared engine.** The now-open [`nyora-shared`](https://github.com/Nyora-Manga/nyora-shared) engine — its source/parser runtime, the loopback REST server, the SQLDelight store, Nyora Cloud sync and the downloads manager — welcomes PRs directly.
- **Help port or request sources.** Suggest a source worth adding, or pitch in on the engine effort described below.
- **Improve or translate docs and the UI.** Sharper README wording, clearer install steps, or translated UI strings make Nyora friendlier to more readers.
- **Star and share.** Genuinely — starring the [repo](https://github.com/Nyora-Manga/nyora-linux/stargazers) and telling a friend helps more people discover a free, ad-free reader.

### Development setup

A quick start to get the Linux app running locally (distinct from the end-user install above):

```bash
# 1. Clone with submodules
git clone --recurse-submodules https://github.com/Nyora-Manga/nyora-linux.git
cd nyora-linux

# 2. Make sure you have JDK 17 or newer (jpackage ships with it)
java -version

# 3. Run the app
./gradlew :desktopApp:run
```

Prerequisites: JDK 17+ (which includes `jpackage`) and Git. Gradle itself is provided by the bundled wrapper (`./gradlew`), so there is nothing else to install.

Cloning with `--recurse-submodules` pulls in the open `nyora-shared` engine alongside this repo, so a full build works out of the box. If you would rather focus on the UI and packaging code here first, you can — the parts that compile against the engine are clearly the `bridge` layer described below, and the shared engine itself is open at [github.com/Nyora-Manga/nyora-shared](https://github.com/Nyora-Manga/nyora-shared) whenever you want to dive deeper. File an issue or PR any time and we are happy to help.

### Where things live

The desktop app source lives under `desktopApp/src/main/kotlin/com/nyora/hasan72341/linux/`:

- `Main.kt`, `AppState.kt` — entry point and top-level application state.
- `ui/` — the Compose UI: `App.kt`, `WelcomeScreen.kt`, `Sidebar.kt`, the `ui/theme/` design system, and `ui/screen/` (one file per screen — `HomeScreen.kt`, `ExploreScreen.kt`, `ReaderScreen.kt`, `SettingsScreen.kt`, `DownloadsScreen.kt`, `BackupScreen.kt`, and more). This is the easiest place for newcomers to make a visible difference.
- `ui/reader/` — reader-specific UI such as `ColorFilter.kt` and `AlternativesDialog.kt`.
- `translate/` — the translation pipeline: `TesseractOcr.kt`, `GoogleTranslate.kt` and `MangaTranslator.kt`.
- `bridge/` — the client that talks to the shared engine over the loopback REST API (`NyoraHttpClient.kt`, `DTOs.kt`). This is the boundary between this repo and the now-open [`nyora-shared`](https://github.com/Nyora-Manga/nyora-shared) engine.

Build and packaging configuration lives in `build.gradle.kts`, `desktopApp/`, `settings.gradle.kts`, and the one-line installer in [`install.sh`](install.sh). Screenshots live in [`docs/screenshots/`](docs/screenshots).

### Good first contributions

Real, repo-grounded places to make a first PR:

- **Tighten a single screen.** Pick one file under `ui/screen/` (say `SettingsScreen.kt` or `HistoryScreen.kt`) and fix a layout, spacing or theming rough edge.
- **Improve installer autodetection.** [`install.sh`](install.sh) maps distro families to artifacts; adding or refining a case for a distro it does not yet recognise is a small, high-value change you can test on your own machine.
- **Strengthen the design system.** Small, consistent tweaks in `ui/theme/DesignSystem.kt` or `ui/theme/Theme.kt` ripple across the whole app.
- **Docs.** Clarify an install step, fix a typo, or document a distro quirk you hit — docs PRs are always welcome.
- **A distro test report.** Run the installer or a package on a distribution and open an issue describing exactly what happened. This needs no code at all.

### The biggest help wanted: porting sources

The single largest contributor opportunity across the whole Nyora project is not in this repo — it is in **`NyoraEngine` (iOS)**, where roughly **1,300 sources** still need porting. The work is mostly mechanical: most sources are thin template subclasses, so the framework and one template are already done (3,659 classes generated, ~1,331 parsers, framework plus one worked template in place), and the remaining sources can largely be filled in by following that template. It is highly parallelisable — many people can each pick a handful of sources and never collide. If that appeals to you, the [nyora-ios](https://github.com/Nyora-Manga/nyora-ios) repository is where to look, and it is the best way to grow the catalogue that every platform — including this one — shares.

On the Linux side, the matching contribution is everything around the engine: testing parser behaviour through the app, reporting sources that misbehave, and improving how the desktop UI surfaces them. The shared `nyora-shared` engine is open too, so parser and runtime fixes can go straight in as PRs there.

### PR & issue etiquette

A few small things keep collaboration smooth and welcoming:

- **Keep PRs focused.** One change per pull request is far easier to review and merge than a large mixed bundle.
- **Describe the change.** A short "what and why", and — for UI work — a before/after screenshot, goes a long way.
- **Be kind.** Reviews are a conversation, not a gate. Questions are welcome, and "I'm new to this" is a perfectly good opening line.
- **Open an issue first for anything large** so we can agree on direction before you invest real time.

Found a bug, have an idea, or want to claim a good-first task? Head to the [Issues page](https://github.com/Nyora-Manga/nyora-linux/issues). And if Nyora makes your reading better, a star on the [repo](https://github.com/Nyora-Manga/nyora-linux/stargazers) and a word to a friend genuinely help other readers find it. Thank you for being here — we are glad you came.

## Acknowledgements

Built and maintained by Md Hasan Raza — [GitHub](https://github.com/Hasan72341) · [Instagram](https://instagram.com/md_hasan_raza____) · [LinkedIn](https://www.linkedin.com/in/md-hasan-raza) · hasanraza96@outlook.com. Sources are powered by the [`nyora-data-driven`](https://github.com/Nyora-Manga/nyora-data-driven) engine. Thanks to the Compose Multiplatform and Tesseract OCR projects, and to the community of readers and contributors who help improve Nyora.

## License

Licensed under the Apache License 2.0 — see [`LICENSE`](LICENSE) for the full text.

> Nyora is not affiliated with any of the manga sources it can access.