#!/usr/bin/env python3
"""
Simple local update server for XB Labs Android Application.
Serves the update JSON metadata endpoint and APK releases.

Usage:
    python server/server.py [port]

Default Port: 8080
Endpoint: http://localhost:8080/api/app/update
"""

import http.server
import json
import hashlib
import os
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
RELEASES_DIR = os.path.join(os.path.dirname(__file__), "releases")

class UpdateServerHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/api/app/update":
            self.handle_update_api()
        elif self.path.startswith("/releases/"):
            self.handle_apk_download()
        else:
            self.send_error(404, "Endpoint Not Found")

    def handle_update_api(self):
        apk_filename = "app-debug.apk"
        apk_path = os.path.join(RELEASES_DIR, apk_filename)

        file_size = 0
        sha256_hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        if os.path.exists(apk_path):
            file_size = os.path.getsize(apk_path)
            sha256 = hashlib.sha256()
            with open(apk_path, "rb") as f:
                for chunk in iter(lambda: f.read(8192), b""):
                    sha256.update(chunk)
            sha256_hash = sha256.hexdigest()

        response_data = {
            "latestVersionCode": 2,
            "latestVersionName": "1.1.0",
            "minimumSupportedVersionCode": 2,
            "forceUpdate": True,
            "apkUrl": f"http://10.0.2.2:{PORT}/releases/{apk_filename}",
            "sha256": sha256_hash,
            "fileSize": file_size,
            "releaseNotes": [
                "Mandatory security and feature update v1.1.0",
                "Enhanced update engine with SHA-256 verification",
                "Performance optimizations"
            ],
            "message": "This update is required to continue using the application."
        }

        json_bytes = json.dumps(response_data, indent=2).encode("utf-8")

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(json_bytes)))
        self.end_headers()
        self.wfile.write(json_bytes)

    def handle_apk_download(self):
        filename = os.path.basename(self.path)
        apk_path = os.path.join(RELEASES_DIR, filename)

        if not os.path.exists(apk_path):
            self.send_error(404, f"APK File {filename} not found in server/releases/")
            return

        file_size = os.path.getsize(apk_path)
        self.send_response(200)
        self.send_header("Content-Type", "application/vnd.android.package-archive")
        self.send_header("Content-Length", str(file_size))
        self.end_headers()

        with open(apk_path, "rb") as f:
            for chunk in iter(lambda: f.read(8192), b""):
                self.wfile.write(chunk)

if __name__ == "__main__":
    os.makedirs(RELEASES_DIR, exist_ok=True)
    server_address = ("", PORT)
    httpd = http.server.HTTPServer(server_address, UpdateServerHandler)
    print(f"🚀 XB Labs Update Server running at http://localhost:{PORT}")
    print(f"📡 API Endpoint: http://localhost:{PORT}/api/app/update")
    print(f"📁 Place Version 2 APK at: {RELEASES_DIR}/app-debug.apk")
    print("Press Ctrl+C to stop.")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")
