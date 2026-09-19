module.exports = function handler(req, res) {
  // CORS Headers
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  const path = require('path');
  const fs = require('fs');
  const crypto = require('crypto');

  // Determine request protocol and host for automatic URL resolution
  const host = req.headers['x-forwarded-host'] || req.headers.host || 'localhost:3000';
  const proto = req.headers['x-forwarded-proto'] || (host.includes('localhost') ? 'http' : 'https');

  let config = {};
  const configPath = path.join(process.cwd(), 'api', 'release-config.json');
  if (fs.existsSync(configPath)) {
    try {
      config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    } catch (e) {
      console.error('Error parsing release-config.json:', e);
    }
  }

  const apkFilename = process.env.APK_FILENAME || config.apkFilename || 'app-1.1.0.apk';
  const apkPath = path.join(process.cwd(), 'public', 'releases', apkFilename);

  let fileSize = parseInt(process.env.APK_FILE_SIZE || (config.fileSize ? String(config.fileSize) : '0'), 10);
  let sha256Hash = process.env.APK_SHA256 || config.sha256 || '';

  if (fs.existsSync(apkPath)) {
    const stats = fs.statSync(apkPath);
    fileSize = stats.size;
    const fileBuffer = fs.readFileSync(apkPath);
    sha256Hash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
  }

  if (!fileSize) fileSize = 15000000;
  if (!sha256Hash) sha256Hash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855';

  const updateResponse = {
    latestVersionCode: parseInt(process.env.LATEST_VERSION_CODE || (config.latestVersionCode ? String(config.latestVersionCode) : '1'), 10),
    latestVersionName: process.env.LATEST_VERSION_NAME || config.latestVersionName || '1.0.0',
    minimumSupportedVersionCode: parseInt(process.env.MINIMUM_SUPPORTED_VERSION_CODE || (config.minimumSupportedVersionCode ? String(config.minimumSupportedVersionCode) : '1'), 10),
    forceUpdate: process.env.FORCE_UPDATE !== undefined ? process.env.FORCE_UPDATE !== 'false' : (config.forceUpdate === true),
    apkUrl: process.env.APK_URL || config.apkUrl || `${proto}://${host}/releases/${apkFilename}`,
    sha256: sha256Hash,
    fileSize: fileSize,
    releaseNotes: config.releaseNotes || [
      'Current stable release',
      'In-app update engine with SHA-256 verification',
      'Performance optimizations'
    ],
    message: config.message || 'This update is required to continue using XB Labs.'
  };

  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Cache-Control', 's-maxage=60, stale-while-revalidate=300');
  res.status(200).json(updateResponse);
};
