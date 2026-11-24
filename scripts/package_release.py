#!/usr/bin/env python3
"""Bungkus source + APK debug menjadi satu ZIP yang bisa dibagikan."""

from __future__ import annotations

import hashlib
import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STAGE = Path("/tmp/opencode/rplayer-release")
OUT = ROOT / "RPlayer-full-debug.zip"


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def copy_file(rel: str) -> None:
    src = ROOT / rel
    if src.is_file():
        dst = STAGE / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)


def copy_dir(rel: str) -> None:
    src = ROOT / rel
    if src.is_dir():
        dst = STAGE / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copytree(src, dst, dirs_exist_ok=True)


def main() -> int:
    apks = sorted((ROOT / "app/build/outputs/apk/debug").glob("*.apk"))
    if not apks:
        print("APK debug belum ada. Jalankan gradle assembleDebug dulu.", file=sys.stderr)
        return 1

    apk = next((p for p in apks if p.name == "app-debug.apk"), apks[0])

    if STAGE.exists():
        shutil.rmtree(STAGE)
    STAGE.mkdir(parents=True)

    for rel in (
        "app/build.gradle.kts",
        "app/proguard-rules.pro",
        "build.gradle.kts",
        "settings.gradle.kts",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
        "README.md",
        "AGENTS.md",
    ):
        copy_file(rel)

    for rel in (
        "app/src",
        "gradle/wrapper",
        "replica",
        "scripts",
    ):
        copy_dir(rel)

    shipped_apk = STAGE / "RPlayer-debug.apk"
    shutil.copy2(apk, shipped_apk)

    notes = f"""RPlayer - paket debug
======================

APK: RPlayer-debug.apk
SHA256 APK: {sha256(shipped_apk)}

Instal
------
adb install -r RPlayer-debug.apk

Atau salin APK ke HP Android, buka file tersebut, lalu izinkan pemasangan
dari sumber tidak dikenal untuk aplikasi ini saja.

Build ulang
-----------
./gradlew assembleDebug

Jika gradlew belum ada, jalankan dulu:
gradle wrapper --gradle-version 8.9

Yang sudah ada di aplikasi ini
------------------------------
- Pemutar audio lokal lewat Media3/ExoPlayer + MediaSessionService
- Daftar lagu dari MediaStore, pencarian, favorit, dan playlist Room
- Home, Search, Library, detail playlist, Player, antrean, dan Settings
- Mini player + notifikasi media dengan kontrol putar/jeda/next/prev

Batasan yang disengaja
----------------------
- Tidak memakai API privat, endpoint, logo, aset, atau tema aplikasi lain.
- Tidak ada streaming online, login, Listen Together, atau Device Connect.
- Ini build debug, bukan build release bertanda tangan untuk Play Store.
- Nama paket sementara id.app.rplayer harus diganti sebelum dijual.
"""
    (STAGE / "BUILD_NOTES.txt").write_text(notes, encoding="utf-8")

    if OUT.exists():
        OUT.unlink()

    with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
        for path in sorted(STAGE.rglob("*")):
            if path.is_file():
                zf.write(path, Path("RPlayer") / path.relative_to(STAGE))

    print(f"ZIP: {OUT}")
    print(f"Ukuran: {OUT.stat().st_size / 1024 / 1024:.1f} MiB")
    print(f"SHA256 ZIP: {sha256(OUT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
