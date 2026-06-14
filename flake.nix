{
  description = "Nyora — a free, open-source manga reader for Linux (Compose Multiplatform desktop)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        lib = pkgs.lib;

        # ── Release coordinates ────────────────────────────────────────────────
        # Bump `version` per release and fill the matching sha256 for your arch.
        # Get a hash with:  nix store prefetch-file <url>   (or nix-prefetch-url)
        version = "1.0.0";

        debUrlFor = arch:
          "https://github.com/Hasan72341/nyora-linux/releases/download/v${version}/Nyora-linux-${arch}.deb";

        # Map Nix systems → the arch suffix used in the release asset names.
        debArch = {
          "x86_64-linux" = "x86_64";
          "aarch64-linux" = "arm64";
        }.${system} or null;

        # Per-(version,arch) sha256 of the .deb. lib.fakeSha256 makes the build
        # fail with the *correct* expected hash printed — paste it back here.
        debSha256 = {
          "x86_64-linux" = lib.fakeSha256;
          "aarch64-linux" = lib.fakeSha256;
        }.${system} or lib.fakeSha256;

        # Runtime libraries a Compose Desktop / Skiko (Skia) + bundled-JRE app
        # needs at runtime. autoPatchelfHook rewrites the bundled JRE + native
        # libs (libskiko, AWT) against these.
        runtimeLibs = with pkgs; [
          stdenv.cc.cc.lib
          zlib
          freetype
          fontconfig
          libGL
          xorg.libX11
          xorg.libXext
          xorg.libXrender
          xorg.libXtst
          xorg.libXi
          xorg.libXrandr
          xorg.libXcursor
          xorg.libXxf86vm
          alsa-lib
          gtk3
          glib
          cairo
          pango
          gdk-pixbuf
        ];

        nyoraPackage =
          if debArch == null then null
          else pkgs.stdenv.mkDerivation rec {
            pname = "nyora";
            inherit version;

            src = pkgs.fetchurl {
              url = debUrlFor debArch;
              sha256 = debSha256;
            };

            nativeBuildInputs = [ pkgs.dpkg pkgs.autoPatchelfHook pkgs.makeWrapper ];
            buildInputs = runtimeLibs;

            # The jpackage .deb installs the app under /opt/<name> with a bundled
            # runtime/ (JRE) and a bin/ launcher. Unpack and relocate it.
            unpackPhase = ''
              runHook preUnpack
              dpkg-deb -x "$src" ./extracted
              runHook postUnpack
            '';

            installPhase = ''
              runHook preInstall
              mkdir -p "$out"
              # jpackage lays the app out under /opt/<pkg>; copy that tree in.
              cp -r ./extracted/opt/* "$out/"

              # Find the real launcher script jpackage generated (its name follows
              # the package name — discover it rather than hardcode).
              launcher="$(find "$out" -maxdepth 3 -type f -path '*/bin/*' \
                          -not -name '*.so' | head -n1)"
              if [ -z "$launcher" ]; then
                echo "ERROR: could not find the Nyora launcher in the .deb payload" >&2
                find "$out" -maxdepth 3 -type f | head -50 >&2
                exit 1
              fi
              chmod +x "$launcher" || true

              mkdir -p "$out/bin"
              makeWrapper "$launcher" "$out/bin/nyora"

              # Desktop entry + icon, if the .deb shipped them.
              if [ -d ./extracted/usr/share ]; then
                mkdir -p "$out/share"
                cp -r ./extracted/usr/share/* "$out/share/" 2>/dev/null || true
              fi
              runHook postInstall
            '';

            meta = with lib; {
              description = "Free, open-source manga reader (Compose Multiplatform desktop)";
              homepage = "https://nyora.pages.dev";
              license = licenses.asl20;
              platforms = [ "x86_64-linux" "aarch64-linux" ];
              mainProgram = "nyora";
              sourceProvenance = [ sourceTypes.binaryNativeCode ];
            };
          };
      in
      {
        # `nix build` / `nix run github:Hasan72341/nyora-linux`
        # NOTE: requires the release .deb to exist and the matching sha256 above
        # to be filled in (the default lib.fakeSha256 intentionally fails and
        # prints the correct hash). Tested pattern; verify on a NixOS host.
        packages = lib.optionalAttrs (nyoraPackage != null) {
          default = nyoraPackage;
          nyora = nyoraPackage;
        };

        apps = lib.optionalAttrs (nyoraPackage != null) {
          default = { type = "app"; program = "${nyoraPackage}/bin/nyora"; };
        };

        # `nix develop` — build/run Nyora from source.
        #   ./gradlew :desktopApp:run                      (dev run)
        #   ./gradlew :desktopApp:packageReleaseDeb        (build the .deb)
        devShells.default = pkgs.mkShell {
          packages = [ pkgs.jdk17 pkgs.gradle ] ++ runtimeLibs;
          # Skiko/AWT dlopen their native libs at runtime; expose them.
          LD_LIBRARY_PATH = lib.makeLibraryPath runtimeLibs;
          shellHook = ''
            echo "Nyora (Linux) dev shell — $(java -version 2>&1 | head -n1)"
            echo "Run:   ./gradlew :desktopApp:run"
            echo "Build: ./gradlew :desktopApp:packageReleaseDeb"
          '';
        };
      });
}
