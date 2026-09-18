const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;
const RELEASES_DIR = path.join(__dirname, 'releases');

if (!fs.existsSync(RELEASES_DIR)) {
  fs.mkdirSync(RELEASES_DIR, { recursive: true });
}

const server = http.createServer((req, res) => {
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
      latestVersionCode: 2,
      latestVersionName: '1.1.0',
      minimumSupportedVersionCode: 2,
      forceUpdate: true,
      apkUrl: `http://10.0.2.2:${PORT}/releases/${apkFilename}`,
      sha256: sha256Hash,
      fileSize: fileSize,
      releaseNotes: [
        'Mandatory security and feature update v1.1.0',
        'Enhanced update engine with SHA-256 verification',
        'Performance optimizations'
      ],
      message: 'This update is required to continue using the application.'
    };

    const jsonString = JSON.stringify(responseData, null, 2);
    res.writeHead(200, {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(jsonString)
    });
    res.end(jsonString);

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

  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Endpoint Not Found');
  }
});

server.listen(PORT, () => {
  console.log(`🚀 XB Labs Update Server running at http://localhost:${PORT}`);
  console.log(`📡 API Endpoint: http://localhost:${PORT}/api/app/update`);
  console.log(`📁 Place Version 2 APK at: ${RELEASES_DIR}/app-debug.apk`);
});
