# XB Labs Android Application with Private Forced-Update System

A real, production-ready native Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

This project implements a **private, mandatory in-app forced update architecture** designed for applications distributed outside of Google Play. It features in-app streaming downloads, SHA-256 hash verification, native PackageInstaller integration, post-installation verification, bypass prevention, and GitHub Actions CI/CD.

---

## 1. Technology Stack

* **Language**: Kotlin 1.9
* **UI Framework**: Jetpack Compose with Material 3 Design System
* **Architecture**: MVVM with Coroutines & `StateFlow`
* **Android SDK**: `minSdk = 26` (Android 8.0), `targetSdk = 34` (Android 14), `compileSdk = 34`
* **Networking**: Pure Kotlin HTTP engine with `HttpURLConnection` on `Dispatchers.IO`
* **Build System**: Modern Gradle with Kotlin DSL (`build.gradle.kts`)
* **CI/CD**: GitHub Actions

---

## 2. Project Architecture & Structure

```text
c:/projects/xb-labs/
├── .github/
│   └── workflows/
│       └── android-build.yml        # CI/CD Workflow for building APK artifacts
├── app/
│   ├── build.gradle.kts             # App-level build dependencies & versioning
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml   # Permissions & FileProvider declaration
│           ├── java/com/xblabs/app/
│           │   ├── MainActivity.kt   # Host activity binding Compose & Lifecycle
│           │   ├── network/
│           │   │   ├── ApiModels.kt  # JSON update response data models
│           │   │   └── ApiService.kt # HTTPS update endpoint communication
│           │   ├── settings/
│           │   │   └── AppConfig.kt  # Configuration & default server endpoint
│           │   ├── update/
│           │   │   ├── ApkDownloader.kt # In-app stream downloader & SHA-256 calculation
│           │   │   ├── ApkInstaller.kt  # Android PackageInstaller & FileProvider launcher
│           │   │   ├── UpdateManager.kt # Central StateMachine & Lifecycle orchestrator
│           │   │   ├── UpdatePolicy.kt  # Evaluator for mandatory/optional/up-to-date logic
│           │   │   ├── UpdateState.kt   # Sealed interface representing UI update states
│           │   │   └── VersionChecker.kt# Utility for reading installed versionCode/Name
│           │   └── ui/
│           │       ├── DownloadScreen.kt       # Live download progress & size UI
│           │       ├── ErrorScreen.kt          # Failure/Retry UI (Network, SHA-256)
│           │       ├── UpdateRequiredScreen.kt # Mandatory update card & notes UI
│           │       └── WelcomeScreen.kt        # Primary welcome screen (when up to date)
│           └── res/
│               ├── values/           # Strings, themes, colors
│               └── xml/file_paths.xml # FileProvider cache paths
├── build.gradle.kts                 # Root project plugins
├── settings.gradle.kts              # Module and repository definitions
└── README.md                        # Documentation
```

---

## 3. How the Private Forced Update System Works

```text
Application Launch
        │
  Read Installed Version (versionCode)
        │
  GET https://example.com/api/app/update
        │
  Evaluate Update Policy
        │
  ┌────────────────────────────────────────────────────────┐
  │                                                        │
  │ Installed versionCode >= latestVersionCode ?           │
  │                                                        │
  ├────────────────────────────┬───────────────────────────┤
  │ YES                        │ NO                        │
  ▼                            ▼                           │
Render WelcomeScreen    Render UpdateRequiredScreen        │
                        (Access Blocked / Back Intercepted)│
                               │                           │
                        [ UPDATE NOW ]                     │
                               │                           │
                        Download APK In-App                │
                        (Live progress & size)             │
                               │                           │
                        Verify SHA-256 Hash                │
                        (Match -> Proceed / Fail -> Delete)│
                               │                           │
                        Launch PackageInstaller             │
                        (ACTION_VIEW via FileProvider)     │
                               │                           │
                        User completes/cancels installer   │
                        App resumes (onResume)             │
                               │                           │
                        Re-verify Installed versionCode    │
                        (versionCode >= required ?)        │
                         /           \                     │
                       YES            NO                   │
                        │              │                   │
                        ▼              ▼                   │
                WelcomeScreen   Mandatory Update Remains   │
└──────────────────────────────────────────────────────────┘
```

---

## 4. In-App APK Download & SHA-256 Integrity Verification

1. **HTTPS Enforcement**: The `ApkDownloader` strictly accepts `https://` URLs (or local test servers) to guarantee data transport security.
2. **Stream Copy**: Streams bytes in chunks through a `DigestInputStream` wrapping `MessageDigest.getInstance("SHA-256")`.
3. **Live Progress**: Emits percentage and downloaded MB / total MB updates to `DownloadScreen` during transfer.
4. **Integrity Check**:
   - Computes the final SHA-256 hex digest upon stream completion.
   - Compares it against the `sha256` value provided in the update API payload.
   - If a mismatch occurs or the stream was corrupted, the invalid file is **deleted immediately** and the app transitions to `UpdateFailed` with a retry option.

---

## 5. Android Package Installation Flow

Android does not allow silent non-root APK installation. The app follows native security standards:

1. **Permission Check**: Verifies `context.packageManager.canRequestPackageInstalls()`. If missing, prompts user with a clear explanation card and provides a direct shortcut to system settings (`Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES`).
2. **FileProvider URI**: Converts the downloaded cache file into a safe `content://` URI using `androidx.core.content.FileProvider`.
3. **Installer Intent**: Launches `Intent.ACTION_VIEW` with `application/vnd.android.package-archive` and `FLAG_GRANT_READ_URI_PERMISSION`.

---

## 6. Bypass Prevention Architecture

* **State Guard**: The `WelcomeScreen` composable is **never composed into the view tree** while an update is pending or failed.
* **Back Button Interception**: `BackHandler(enabled = true)` intercepts the system back button on mandatory screens (`UpdateRequiredScreen`, `DownloadScreen`, `ErrorScreen`), preventing users from escaping to the main app.
* **Lifecycle Re-checking**: When the app resumes (`onResume`), `UpdateManager` reads the actual system `PackageManager` version code again to verify whether an update was genuinely installed.

---

## 7. Server Update API Specification

### Endpoint: `GET /api/app/update`

#### Example Response:

```json
{
  "latestVersionCode": 2,
  "latestVersionName": "1.1.0",
  "minimumSupportedVersionCode": 2,
  "forceUpdate": true,
  "apkUrl": "https://updates.example.com/releases/app-v1.1.0.apk",
  "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "fileSize": 48234412,
  "releaseNotes": [
    "Performance improvements",
    "Enhanced security checks"
  ],
  "message": "This update is required to continue using the application."
```

---

### Vercel Deployment (One-Click / Instant Serverless API)

The project includes built-in Vercel configuration ([vercel.json](file:///c:/projects/xb-labs/vercel.json) and [api/update.js](file:///c:/projects/xb-labs/api/update.js)).

#### 1. Deploy via Vercel CLI or GitHub
```bash
npx vercel
```
Or connect this GitHub repository directly to Vercel via the Vercel Dashboard.

#### 2. Vercel Endpoint
Vercel routes requests automatically to:
`https://your-project.vercel.app/api/app/update`

#### 3. Update App Version via Vercel Environment Variables
You can update target versions and APK URLs instantly in Vercel **Project Settings > Environment Variables** without modifying code:
- `LATEST_VERSION_CODE` (e.g. `2`)
- `LATEST_VERSION_NAME` (e.g. `1.1.0`)
- `MINIMUM_SUPPORTED_VERSION_CODE` (e.g. `2`)
- `FORCE_UPDATE` (`true` or `false`)
- `APK_URL` (e.g. `https://your-cdn.com/app-1.1.0.apk` or `https://your-project.vercel.app/releases/app-1.1.0.apk`)
- `APK_SHA256` (e.g. `e3b0c44298fc1c14...`)
- `APK_FILE_SIZE` (e.g. `52428800`)

---


## 8. GitHub Actions CI/CD Workflow

The workflow file is located at `.github/workflows/android-build.yml`.

### Workflow Capabilities:
* Triggers automatically on `push` to `main`/`master` or via manual execution (`workflow_dispatch`).
* Configures Java 17 and Android SDK build tools.
* Executes `./gradlew assembleDebug` to compile the app and produce the APK artifact.
* Uploads the resulting APK as a downloadable GitHub Actions artifact (`app-debug-apk`).

### Configuring Release Signing via Secrets:
To build signed release APKs in GitHub Actions, configure the following repository secrets under **Settings > Secrets and variables > Actions**:

* `RELEASE_KEYSTORE_BASE64`: Base64 string of your `.keystore` file (`base64 release.keystore`).
* `RELEASE_KEYSTORE_PASSWORD`: Keystore password.
* `RELEASE_KEY_ALIAS`: Key alias name.
* `RELEASE_KEY_PASSWORD`: Key password.

---

## 9. Private Version 1 -> Version 2 Testing Guide (Without Google Play)

### Step 1: Build Version 1
1. In `app/build.gradle.kts`, set:
   ```kotlin
   versionCode = 1
   versionName = "1.0.0"
   ```
2. Build and install Version 1 onto your Android test device/emulator:
   ```bash
   ./gradlew installDebug
   ```

### Step 2: Build Version 2
1. In `app/build.gradle.kts`, increment:
   ```kotlin
   versionCode = 2
   versionName = "1.1.0"
   ```
2. Build the Version 2 APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Calculate the SHA-256 hash of the generated Version 2 APK:
   ```powershell
   Get-FileHash app\build\outputs\apk\debug\app-debug.apk -Algorithm SHA256
   ```

### Step 3: Deploy to Update Server
1. Upload `app-debug.apk` to your HTTPS server directory (e.g., `https://updates.example.com/app-1.1.0.apk`).
2. Update your API server `/api/app/update` response:
   ```json
   {
     "latestVersionCode": 2,
     "latestVersionName": "1.1.0",
     "minimumSupportedVersionCode": 2,
     "forceUpdate": true,
     "apkUrl": "https://updates.example.com/app-1.1.0.apk",
     "sha256": "<YOUR_SHA256_HASH>",
     "fileSize": <EXACT_FILE_SIZE_BYTES>,
     "releaseNotes": ["Feature updates"],
     "message": "Update required."
   }
   ```

### Step 4: Test Update Execution
1. Launch Version 1 on the test device.
2. The app contacts the update server and detects Version 2.
3. Access to Version 1 content is **blocked**, displaying `UpdateRequiredScreen`.
4. Press **UPDATE NOW**. The app downloads the APK in-app with live progress.
5. The SHA-256 hash is verified.
6. Android PackageInstaller opens. Confirm installation.
7. The app re-launches / resumes. The installed version is verified as `2`.
8. The `WelcomeScreen` appears!

---

## 10. Documented Test Cases

| Test Case | Scenario | Expected Behavior |
| :--- | :--- | :--- |
| **Test 1** | Installed = 2, Server = 2 | `WelcomeScreen` is rendered immediately. |
| **Test 2** | Installed = 1, Minimum = 2, Force = true | App blocks, downloads APK, verifies SHA-256, launches installer, verifies Version 2 on resume. |
| **Test 3** | User cancels installer prompt | App returns to foreground on `onResume`, detects `versionCode` is still 1, remains blocked on update screen. |
| **Test 4** | Bad SHA-256 (Hash mismatch) | APK download completes, hash fails check, invalid file is deleted, `ErrorScreen` is shown with Retry option. |
| **Test 5** | Offline server / Network error | Fail-closed policy prevents false "up to date" state; displays `ErrorScreen` with retry capability. |
| **Test 6** | App killed during download | On app re-launch, update check runs and download restarts cleanly without corrupted state. |
| **Test 7** | App killed after installer opens | On app re-launch, `VersionChecker` reads system package manager; if update succeeded, opens app; otherwise stays blocked. |
| **Test 8** | Invalid or corrupt APK payload | Downloader rejects non-HTTPS or corrupt streams before system installer is invoked. |

---

## 11. Security Model & Signing Key Constraint

> [!IMPORTANT]
> **Android Signing Identity Requirement**: Android strictly requires that any app update must be signed with the **exact same signing certificate** as the currently installed app. If Version 2 is signed with a different key than Version 1, Android's PackageInstaller will fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Ensure both builds share the same debug or production release keystore.
