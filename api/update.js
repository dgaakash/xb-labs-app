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

  // Determine request protocol and host for automatic URL resolution
  const host = req.headers['x-forwarded-host'] || req.headers.host || 'localhost:3000';
  const proto = req.headers['x-forwarded-proto'] || (host.includes('localhost') ? 'http' : 'https');

  const updateResponse = {
    latestVersionCode: parseInt(process.env.LATEST_VERSION_CODE || '2', 10),
    latestVersionName: process.env.LATEST_VERSION_NAME || '1.1.0',
    minimumSupportedVersionCode: parseInt(process.env.MINIMUM_SUPPORTED_VERSION_CODE || '2', 10),
    forceUpdate: process.env.FORCE_UPDATE !== 'false',
    apkUrl: process.env.APK_URL || `${proto}://${host}/releases/app-1.1.0.apk`,
    sha256: process.env.APK_SHA256 || 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    fileSize: parseInt(process.env.APK_FILE_SIZE || '52428800', 10),
    releaseNotes: [
      'Mandatory security & feature update v1.1.0',
      'Enhanced in-app update engine with SHA-256 verification',
      'Performance optimizations'
    ],
    message: 'This update is required to continue using XB Labs.'
  };

  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Cache-Control', 's-maxage=60, stale-while-revalidate=300');
  res.status(200).json(updateResponse);
};
