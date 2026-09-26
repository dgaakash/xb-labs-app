const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;
const RELEASES_DIR = path.join(__dirname, 'releases');
const DB_FILE = path.join(__dirname, 'crm_store.json');

if (!fs.existsSync(RELEASES_DIR)) {
  fs.mkdirSync(RELEASES_DIR, { recursive: true });
}

// Default initial state for server DB
const defaultDbState = {
  users: [
    {
      id: 'usr_admin_xavier',
      name: 'Xavier',
      email: 'xavier@xblabs.com',
      username: 'xavier',
      role: 'ADMIN',
      passwordHash: 'xblabs123@@@@',
      theme: 'default',
      active: true,
      createdAt: Date.now(),
      lastActive: Date.now()
    },
    {
      id: 'usr_emp_blessi',
      name: 'Blessi',
      email: 'blessi@xblabs.com',
      username: 'blessi',
      role: 'EMPLOYEE',
      passwordHash: 'xblabs123@',
      theme: 'pink-princess',
      active: true,
      createdAt: Date.now(),
      lastActive: Date.now()
    }
  ],
  clients: [],
  callRecords: [],
  followUps: [],
  importBatches: [],
  notifications: [],
  activityLogs: []
};

function loadDb() {
  if (fs.existsSync(DB_FILE)) {
    try {
      return JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
    } catch (e) {
      console.error('Error reading CRM DB file, resetting:', e);
    }
  }
  saveDb(defaultDbState);
  return defaultDbState;
}

function saveDb(db) {
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
  } catch (e) {
    console.error('Error writing CRM DB file:', e);
  }
}

let dbState = loadDb();

function helperSendJson(res, statusCode, data) {
  const jsonStr = JSON.stringify(data);
  res.writeHead(statusCode, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Content-Length': Buffer.byteLength(jsonStr)
  });
  res.end(jsonStr);
}

const server = http.createServer((req, res) => {
  // CORS Preflight handling
  if (req.method === 'OPTIONS') {
    res.writeHead(200, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    res.end();
    return;
  }

  // 1. UPDATE CHECK ENDPOINT
  if (req.url === '/api/app/update' && req.method === 'GET') {
    const apkFilename = 'app-debug.apk';
    const apkPath = path.join(RELEASES_DIR, apkFilename);

    let fileSize = 0;
    let sha256Hash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855';

    if (fs.existsSync(apkPath)) {
      const stats = fs.statSync(apkPath);
      fileSize = stats.size;
      const fileBuffer = fs.readFileSync(apkPath);
      sha256Hash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
    }

    const responseData = {
      latestVersionCode: 4,
      latestVersionName: '4.1.0',
      minimumSupportedVersionCode: 4,
      forceUpdate: true,
      apkUrl: `http://10.0.2.2:${PORT}/releases/${apkFilename}`,
      sha256: sha256Hash,
      fileSize: fileSize,
      releaseNotes: [
        'XB Labs Sales Engine v4.1.0',
        'Cross-device CRM synchronization engine',
        'Mandatory update'
      ],
      message: 'This update is required to continue using the application.'
    };

    helperSendJson(res, 200, responseData);

  // 2. APK DOWNLOAD ENDPOINT
  } else if (req.url.startsWith('/releases/') && req.method === 'GET') {
    const filename = path.basename(req.url);
    const apkPath = path.join(RELEASES_DIR, filename);

    if (!fs.existsSync(apkPath)) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end(`APK File ${filename} not found in server/releases/`);
      return;
    }

    const stats = fs.statSync(apkPath);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': stats.size
    });

    const readStream = fs.createReadStream(apkPath);
    readStream.pipe(res);

  // 3. CRM SYNC ENDPOINT (GET)
  } else if (req.url.startsWith('/api/crm/sync') && req.method === 'GET') {
    helperSendJson(res, 200, dbState);

  // 4. CRM IMPORT ENDPOINT (POST)
  } else if (req.url === '/api/crm/import' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk.toString());
    req.on('end', () => {
      try {
        const payload = JSON.parse(body);
        const adminId = payload.adminId || 'usr_admin_xavier';
        const adminName = payload.adminName || 'Xavier';
        const sourceName = payload.sourceName || 'JSON Import';
        const records = payload.records || [];

        const batchId = 'batch_' + crypto.randomBytes(4).toString('hex');
        const now = Date.now();
        const activeEmployees = dbState.users.filter(u => u.role === 'EMPLOYEE' && u.active);

        // Calculate workload
        const activeWorkloadMap = {};
        activeEmployees.forEach(emp => {
          activeWorkloadMap[emp.id] = dbState.clients.filter(c =>
            c.assignedEmployeeId === emp.id &&
            c.employeeWorkflowStatus === 'ACTIVE' &&
            ['NEW', 'IN_PROGRESS', 'FOLLOW_UP'].includes(c.currentStatus)
          ).length;
        });

        const assignedCounts = {};
        activeEmployees.forEach(emp => assignedCounts[emp.id] = 0);
        const newClients = [];

        records.forEach(rec => {
          let assignedEmp = null;
          if (activeEmployees.length > 0) {
            let lowestEmpId = activeEmployees[0].id;
            let lowestCount = activeWorkloadMap[lowestEmpId] || 0;
            activeEmployees.forEach(emp => {
              if ((activeWorkloadMap[emp.id] || 0) < lowestCount) {
                lowestCount = activeWorkloadMap[emp.id];
                lowestEmpId = emp.id;
              }
            });
            assignedEmp = activeEmployees.find(e => e.id === lowestEmpId);
            if (assignedEmp) {
              activeWorkloadMap[assignedEmp.id] = (activeWorkloadMap[assignedEmp.id] || 0) + 1;
              assignedCounts[assignedEmp.id] = (assignedCounts[assignedEmp.id] || 0) + 1;
            }
          }

          const client = {
            id: 'cli_' + crypto.randomBytes(4).toString('hex'),
            businessName: rec.businessName || 'Business Lead',
            category: rec.category || 'General',
            rating: parseFloat(rec.rating) || 0.0,
            reviewCount: parseInt(rec.reviewCount) || 0,
            phone: rec.phone || '',
            normalizedPhone: rec.normalizedPhone || rec.phone || '',
            website: rec.website || '',
            address: rec.address || '',
            mapsUrl: rec.mapsUrl || '',
            assignedEmployeeId: assignedEmp ? assignedEmp.id : null,
            assignedEmployeeName: assignedEmp ? assignedEmp.name : null,
            currentStatus: 'NEW',
            employeeWorkflowStatus: 'ACTIVE',
            pipelineStage: 'NEW_LEAD',
            priority: 'NORMAL',
            followUpCount: 0,
            nextFollowUpDate: null,
            lastContactedAt: null,
            createdAt: now,
            importedAt: now,
            importBatchId: batchId,
            archivedAt: null
          };

          dbState.clients.push(client);
          newClients.push(client);
        });

        // Summary builder
        let summaryText = '';
        Object.keys(assignedCounts).forEach(empId => {
          const empName = activeEmployees.find(e => e.id === empId)?.name || 'Employee';
          summaryText += `${empName}: ${assignedCounts[empId]} clients; `;
        });

        const batch = {
          id: batchId,
          adminId: adminId,
          adminName: adminName,
          createdAt: now,
          sourceName: sourceName,
          totalRecords: records.length,
          importedRecords: newClients.length,
          duplicateRecords: 0,
          invalidRecords: 0,
          distributionSummary: summaryText.trim()
        };
        dbState.importBatches.unshift(batch);

        // Create Notifications for employees
        Object.keys(assignedCounts).forEach(empId => {
          const count = assignedCounts[empId];
          if (count > 0) {
            dbState.notifications.unshift({
              id: 'notif_' + crypto.randomBytes(4).toString('hex'),
              userId: empId,
              type: 'NEW_JOBS',
              title: 'New Clients Assigned',
              message: `You received ${count} new clients from batch '${sourceName}'.`,
              isRead: false,
              createdAt: now
            });
          }
        });

        saveDb(dbState);
        helperSendJson(res, 200, dbState);
      } catch (err) {
        console.error('Error importing leads:', err);
        helperSendJson(res, 400, { error: err.message });
      }
    });

  // 5. CRM CALL OUTCOME ENDPOINT (POST)
  } else if (req.url === '/api/crm/call-outcome' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk.toString());
    req.on('end', () => {
      try {
        const payload = JSON.parse(body);
        const { clientId, employeeId, employeeName, outcome, notes, customFollowUpDate } = payload;
        const now = Date.now();

        const client = dbState.clients.find(c => c.id === clientId);
        if (!client) {
          return helperSendJson(res, 404, { error: 'Client not found' });
        }

        const currentFollowUpNum = client.followUpCount || 0;

        const callRecord = {
          id: 'call_' + crypto.randomBytes(4).toString('hex'),
          clientId: client.id,
          clientName: client.businessName,
          employeeId: employeeId,
          employeeName: employeeName,
          statusOutcome: outcome,
          notes: notes || '',
          timestamp: now,
          followUpNumber: currentFollowUpNum
        };
        dbState.callRecords.unshift(callRecord);

        if (outcome === 'DIDNT_PICK_UP' || outcome === 'MAYBE_INTERESTED') {
          const nextAttemptNum = currentFollowUpNum + 1;
          if (nextAttemptNum <= 3) {
            const dueDate = customFollowUpDate || (now + 86400000);
            const followUp = {
              id: 'fol_' + crypto.randomBytes(4).toString('hex'),
              clientId: client.id,
              clientName: client.businessName,
              clientPhone: client.phone,
              employeeId: employeeId,
              employeeName: employeeName,
              attemptNumber: nextAttemptNum,
              dueDate: dueDate,
              state: 'PENDING',
              notes: notes || '',
              previousResult: outcome,
              createdAt: now,
              completedAt: null
            };
            dbState.followUps.unshift(followUp);

            client.currentStatus = 'FOLLOW_UP';
            client.employeeWorkflowStatus = 'ACTIVE';
            client.followUpCount = nextAttemptNum;
            client.nextFollowUpDate = dueDate;
            client.lastContactedAt = now;
          } else {
            client.currentStatus = 'CLOSED';
            client.employeeWorkflowStatus = 'EXHAUSTED';
            client.followUpCount = nextAttemptNum;
            client.nextFollowUpDate = null;
            client.lastContactedAt = now;
          }
        } else if (outcome === 'INTERESTED') {
          client.currentStatus = 'INTERESTED';
          client.employeeWorkflowStatus = 'ACTIVE';
          client.pipelineStage = 'INTERESTED';
          client.priority = 'HIGH';
          client.nextFollowUpDate = null;
          client.lastContactedAt = now;
        } else if (outcome === 'NOT_INTERESTED') {
          client.currentStatus = 'NOT_INTERESTED';
          client.employeeWorkflowStatus = 'CLOSED';
          client.nextFollowUpDate = null;
          client.lastContactedAt = now;
        }

        saveDb(dbState);
        helperSendJson(res, 200, dbState);
      } catch (err) {
        console.error('Error recording call outcome:', err);
        helperSendJson(res, 400, { error: err.message });
      }
    });

  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Endpoint Not Found');
  }
});

server.listen(PORT, () => {
  console.log(`🚀 XB Labs CRM & Update Server running at http://localhost:${PORT}`);
  console.log(`📡 Update API: http://localhost:${PORT}/api/app/update`);
  console.log(`📡 CRM Sync API: http://localhost:${PORT}/api/crm/sync`);
  console.log(`📡 CRM Import API: http://localhost:${PORT}/api/crm/import`);
});
