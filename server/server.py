#!/usr/bin/env python3
"""
XB Labs Server with Update Engine & Cross-Device CRM Data Synchronization.

Usage:
    python server/server.py [port]

Default Port: 8080
"""

import http.server
import json
import hashlib
import os
import sys
import time
import uuid

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
RELEASES_DIR = os.path.join(os.path.dirname(__file__), "releases")
DB_FILE = os.path.join(os.path.dirname(__file__), "crm_store.json")

os.makedirs(RELEASES_DIR, exist_ok=True)

DEFAULT_DB = {
    "users": [
        {
            "id": "usr_admin_xavier",
            "name": "Xavier",
            "email": "xavier@xblabs.com",
            "username": "xavier",
            "role": "ADMIN",
            "passwordHash": "xblabs123@@@@",
            "theme": "default",
            "active": True,
            "createdAt": int(time.time() * 1000),
            "lastActive": int(time.time() * 1000)
        },
        {
            "id": "usr_emp_blessi",
            "name": "Blessi",
            "email": "blessi@xblabs.com",
            "username": "blessi",
            "role": "EMPLOYEE",
            "passwordHash": "xblabs123@",
            "theme": "pink-princess",
            "active": True,
            "createdAt": int(time.time() * 1000),
            "lastActive": int(time.time() * 1000)
        }
    ],
    "clients": [],
    "callRecords": [],
    "followUps": [],
    "importBatches": [],
    "notifications": [],
    "activityLogs": []
}

def load_db():
    if os.path.exists(DB_FILE):
        try:
            with open(DB_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading DB, resetting: {e}")
    save_db(DEFAULT_DB)
    return DEFAULT_DB

def save_db(db):
    try:
        with open(DB_FILE, "w", encoding="utf-8") as f:
            json.dump(db, f, indent=2)
    except Exception as e:
        print(f"Error saving DB: {e}")

db_state = load_db()

class CrmServerHandler(http.server.SimpleHTTPRequestHandler):
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self):
        if self.path == "/api/app/update":
            self.handle_update_api()
        elif self.path.startswith("/api/crm/sync"):
            self.send_json_response(200, db_state)
        elif self.path.startswith("/releases/"):
            self.handle_apk_download()
        else:
            self.send_error(404, "Endpoint Not Found")

    def do_POST(self):
        content_len = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_len) if content_len > 0 else b"{}"
        
        try:
            payload = json.loads(post_data.decode("utf-8"))
        except Exception:
            payload = {}

        if self.path == "/api/crm/import":
            self.handle_import(payload)
        elif self.path == "/api/crm/call-outcome":
            self.handle_call_outcome(payload)
        else:
            self.send_error(404, "Endpoint Not Found")

    def send_json_response(self, status, data):
        json_bytes = json.dumps(data, indent=2).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Content-Length", str(len(json_bytes)))
        self.end_headers()
        self.wfile.write(json_bytes)

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
            "latestVersionCode": 4,
            "latestVersionName": "4.1.0",
            "minimumSupportedVersionCode": 4,
            "forceUpdate": True,
            "apkUrl": f"http://10.0.2.2:{PORT}/releases/{apk_filename}",
            "sha256": sha256_hash,
            "fileSize": file_size,
            "releaseNotes": [
                "XB Labs Sales Engine v4.1.0",
                "Cross-device CRM synchronization engine",
                "Mandatory update"
            ],
            "message": "This update is required to continue using the application."
        }

        self.send_json_response(200, response_data)

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

    def handle_import(self, payload):
        admin_id = payload.get("adminId", "usr_admin_xavier")
        admin_name = payload.get("adminName", "Xavier")
        source_name = payload.get("sourceName", "JSON Import")
        records = payload.get("records", [])

        batch_id = "batch_" + uuid.uuid4().hex[:8]
        now = int(time.time() * 1000)

        active_emps = [u for u in db_state["users"] if u.get("role") == "EMPLOYEE" and u.get("active")]

        workload = {}
        for emp in active_emps:
            workload[emp["id"]] = len([
                c for c in db_state["clients"]
                if c.get("assignedEmployeeId") == emp["id"]
                and c.get("employeeWorkflowStatus") == "ACTIVE"
                and c.get("currentStatus") in ["NEW", "IN_PROGRESS", "FOLLOW_UP"]
            ])

        assigned_counts = {emp["id"]: 0 for emp in active_emps}

        for rec in records:
            assigned_emp = None
            if active_emps:
                target_emp_id = min(workload.keys(), key=lambda k: workload[k]) if workload else None
                assigned_emp = next((e for e in active_emps if e["id"] == target_emp_id), None)
                if assigned_emp:
                    workload[assigned_emp["id"]] += 1
                    assigned_counts[assigned_emp["id"]] += 1

            client = {
                "id": "cli_" + uuid.uuid4().hex[:8],
                "businessName": rec.get("businessName", "Business Lead"),
                "category": rec.get("category", "General"),
                "rating": float(rec.get("rating", 0.0)),
                "reviewCount": int(rec.get("reviewCount", 0)),
                "phone": rec.get("phone", ""),
                "normalizedPhone": rec.get("normalizedPhone", rec.get("phone", "")),
                "website": rec.get("website", ""),
                "address": rec.get("address", ""),
                "mapsUrl": rec.get("mapsUrl", ""),
                "assignedEmployeeId": assigned_emp["id"] if assigned_emp else None,
                "assignedEmployeeName": assigned_emp["name"] if assigned_emp else None,
                "currentStatus": "NEW",
                "employeeWorkflowStatus": "ACTIVE",
                "pipelineStage": "NEW_LEAD",
                "priority": "NORMAL",
                "followUpCount": 0,
                "nextFollowUpDate": None,
                "lastContactedAt": None,
                "createdAt": now,
                "importedAt": now,
                "importBatchId": batch_id,
                "archivedAt": None
            }
            db_state["clients"].append(client)

        summary_text = " ".join([f"{emp['name']}: {assigned_counts[emp['id']]} clients;" for emp in active_emps])

        batch = {
            "id": batch_id,
            "adminId": admin_id,
            "adminName": admin_name,
            "createdAt": now,
            "sourceName": source_name,
            "totalRecords": len(records),
            "importedRecords": len(records),
            "duplicateRecords": 0,
            "invalidRecords": 0,
            "distributionSummary": summary_text.strip()
        }
        db_state["importBatches"].insert(0, batch)

        for emp_id, count in assigned_counts.items():
            if count > 0:
                db_state["notifications"].insert(0, {
                    "id": "notif_" + uuid.uuid4().hex[:8],
                    "userId": emp_id,
                    "type": "NEW_JOBS",
                    "title": "New Clients Assigned",
                    "message": f"You received {count} new clients from batch '{source_name}'.",
                    "isRead": False,
                    "createdAt": now
                })

        save_db(db_state)
        self.send_json_response(200, db_state)

    def handle_call_outcome(self, payload):
        client_id = payload.get("clientId")
        employee_id = payload.get("employeeId")
        employee_name = payload.get("employeeName")
        outcome = payload.get("outcome")
        notes = payload.get("notes", "")
        custom_date = payload.get("customFollowUpDate")

        now = int(time.time() * 1000)

        client = next((c for c in db_state["clients"] if c["id"] == client_id), None)
        if not client:
            return self.send_json_response(404, {"error": "Client not found"})

        cur_fu_num = client.get("followUpCount", 0)

        db_state["callRecords"].insert(0, {
            "id": "call_" + uuid.uuid4().hex[:8],
            "clientId": client["id"],
            "clientName": client["businessName"],
            "employeeId": employee_id,
            "employeeName": employee_name,
            "statusOutcome": outcome,
            "notes": notes,
            "timestamp": now,
            "followUpNumber": cur_fu_num
        })

        if outcome in ["DIDNT_PICK_UP", "MAYBE_INTERESTED"]:
            next_num = cur_fu_num + 1
            if next_num <= 3:
                due = custom_date or (now + 86400000)
                db_state["followUps"].insert(0, {
                    "id": "fol_" + uuid.uuid4().hex[:8],
                    "clientId": client["id"],
                    "clientName": client["businessName"],
                    "clientPhone": client.get("phone", ""),
                    "employeeId": employee_id,
                    "employeeName": employee_name,
                    "attemptNumber": next_num,
                    "dueDate": due,
                    "state": "PENDING",
                    "notes": notes,
                    "previousResult": outcome,
                    "createdAt": now,
                    "completedAt": None
                })
                client["currentStatus"] = "FOLLOW_UP"
                client["employeeWorkflowStatus"] = "ACTIVE"
                client["followUpCount"] = next_num
                client["nextFollowUpDate"] = due
                client["lastContactedAt"] = now
            else:
                client["currentStatus"] = "CLOSED"
                client["employeeWorkflowStatus"] = "EXHAUSTED"
                client["followUpCount"] = next_num
                client["nextFollowUpDate"] = None
                client["lastContactedAt"] = now
        elif outcome == "INTERESTED":
            client["currentStatus"] = "INTERESTED"
            client["employeeWorkflowStatus"] = "ACTIVE"
            client["pipelineStage"] = "INTERESTED"
            client["priority"] = "HIGH"
            client["nextFollowUpDate"] = None
            client["lastContactedAt"] = now
        elif outcome == "NOT_INTERESTED":
            client["currentStatus"] = "NOT_INTERESTED"
            client["employeeWorkflowStatus"] = "CLOSED"
            client["nextFollowUpDate"] = None
            client["lastContactedAt"] = now

        save_db(db_state)
        self.send_json_response(200, db_state)

if __name__ == "__main__":
    server_address = ("", PORT)
    httpd = http.server.HTTPServer(server_address, CrmServerHandler)
    print(f"🚀 XB Labs Server running at http://localhost:{PORT}")
    print(f"📡 Update API: http://localhost:{PORT}/api/app/update")
    print(f"📡 CRM Sync API: http://localhost:{PORT}/api/crm/sync")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")
