I want you to build me a **real Android mobile application from scratch**, using a modern native Android stack.

This is a **PRIVATE application**.

I do NOT want to publish it on Google Play.

I do NOT want to use Google Play In-App Updates.

I want my own **private APK distribution and forced-update system**.

The application itself should initially be extremely simple — basically just a polished welcome screen — but the **update architecture must be real, robust, and production-ready** because that is the main purpose of this project.

---

# CORE REQUIREMENT

Build an Android app that can:

1. Launch.
2. Check my own update API/server for the latest version.
3. Compare the installed version against the server's required version.
4. If the installed version is below the minimum supported version:

   * BLOCK access to the application.
   * Display a mandatory update screen.
   * Download the new APK directly from inside the app.
   * Do NOT open Chrome.
   * Do NOT open a website.
   * Do NOT redirect the user to Google Play.
   * Do NOT require another downloader application.
   * Do NOT allow the user to skip the update.
5. After downloading, invoke Android's package installation flow.
6. Detect whether the update actually succeeded.
7. Re-check the installed version.
8. Only allow the user into the app once the required version is installed.

The normal application UI must remain inaccessible while a mandatory update is unresolved.

---

# VERY IMPORTANT ANDROID LIMITATION

I understand that a normal Android application cannot silently install/update an APK without Android's package installer/security confirmation.

Therefore:

**DO NOT pretend that silent installation is possible.**

The desired experience is:

```text
APP
 ↓
Update check
 ↓
Mandatory update
 ↓
Download APK INSIDE APP
 ↓
Android installation confirmation
 ↓
Install
 ↓
Return to APP
 ↓
Verify new version
 ↓
Welcome screen
```

The browser should NOT be part of the workflow.

The download itself must happen through the application.

Use the correct modern Android APIs for downloading and launching the APK installation process.

Handle Android versions and the appropriate package-install permissions correctly.

---

# TECHNOLOGY

Use:

* Kotlin
* Jetpack Compose
* Material 3
* AndroidX
* Coroutines
* ViewModel where appropriate
* Lifecycle-aware components
* Modern Gradle
* Modern Android SDK/API configuration
* Clean separation between UI, update logic, networking, and installation handling

Use current stable libraries available in the environment.

Before implementing anything dependent on current Android APIs, verify the current recommended implementation.

Do not use obsolete/deprecated APIs unless there is no practical alternative.

---

# APPLICATION STRUCTURE

Use a sensible structure such as:

```text
app/
  src/main/
    java/com/example/app/
      MainActivity.kt

      ui/
        WelcomeScreen.kt
        UpdateRequiredScreen.kt
        DownloadScreen.kt
        ErrorScreen.kt

      update/
        UpdateManager.kt
        UpdateRepository.kt
        UpdateState.kt
        UpdatePolicy.kt
        ApkDownloader.kt
        ApkInstaller.kt
        VersionChecker.kt

      network/
        ApiService.kt
        ApiModels.kt

      settings/
        AppConfig.kt
```

You can improve this structure if you have a better architecture.

Do NOT add pointless abstractions.

---

# STARTUP FLOW

The app startup flow should be:

```text
Application Launch
        ↓
Initialize App
        ↓
Read Installed Version
        ↓
Call Update API
        ↓
Validate Update Response
        ↓
Compare Version
        ↓
┌───────────────────────────────┐
│                               │
│ Update Required?              │
│                               │
├───────────────┬───────────────┤
│ NO            │ YES           │
│               │               │
↓               ↓               │
Welcome       Update Screen     │
                ↓               │
           Download APK        │
                ↓               │
           Install APK         │
                ↓               │
          Verify Version       │
                ↓               │
             Welcome            │
└───────────────────────────────┘
```

The welcome screen must NEVER be shown before a mandatory update has been resolved.

---

# UPDATE API

Design the app to communicate with my own HTTPS API.

For example:

```text
GET https://example.com/api/app/update
```

The API can return:

```json
{
  "latestVersionCode": 5,
  "latestVersionName": "1.4.0",
  "minimumSupportedVersionCode": 5,
  "forceUpdate": true,
  "apkUrl": "https://example.com/releases/app-1.4.0.apk",
  "sha256": "PUT_REAL_SHA256_HERE",
  "fileSize": 52428800,
  "releaseNotes": [
    "Bug fixes",
    "Performance improvements"
  ],
  "message": "This update is required to continue using the app."
}
```

Create proper Kotlin data classes for this response.

Do not hard-code the update version into the UI.

---

# UPDATE POLICY

Implement the following logic:

### Current version >= latestVersionCode

```text
No update required
→ Open app
```

### Current version < latestVersionCode

and:

```text
forceUpdate = true
```

or:

```text
currentVersionCode < minimumSupportedVersionCode
```

Then:

```text
Mandatory update
→ Block app
→ Show update UI
```

Later I should be able to support optional updates as well.

Create an update policy abstraction that can distinguish:

```text
UP_TO_DATE
OPTIONAL_UPDATE
MANDATORY_UPDATE
UPDATE_ERROR
```

---

# VERSION COMPARISON

Use Android's actual:

```text
versionCode
versionName
```

for the installed application.

Prefer `versionCode` for machine comparison.

Example:

```text
Installed:
versionCode = 1
versionName = 1.0.0

Server:
latestVersionCode = 2
latestVersionName = 1.1.0
minimumSupportedVersionCode = 2
```

Result:

```text
MANDATORY UPDATE
```

---

# DOWNLOAD APK INSIDE THE APP

When the user presses:

```text
UPDATE NOW
```

the app should download the APK itself.

Requirements:

* HTTPS only
* Show download progress
* Show downloaded size / total size where available
* Handle unknown content length
* Handle network interruptions
* Support retry
* Avoid corrupt partial downloads
* Save to an appropriate app-controlled/cache/files location
* Don't unnecessarily expose the APK as a public downloadable file on the device
* Verify the downloaded file before attempting installation

For example:

```text
Downloading update…

68%

34.2 MB / 50.0 MB
```

Do NOT open an external browser.

Do NOT launch a generic download manager.

The app should own the download operation.

---

# APK INTEGRITY

The update response contains a SHA-256 value.

After downloading:

1. Calculate the APK SHA-256.
2. Compare it with the server-provided SHA-256.
3. If it doesn't match:

   * Reject the APK.
   * Delete the invalid file.
   * Show an error.
   * Allow retry.
4. Never launch installation for a corrupted/mismatched APK.

Implement this properly.

---

# APK INSTALLATION

Use the appropriate Android package installation mechanism.

The app should launch the system installation UI for the downloaded APK.

Handle the requirements associated with installing packages from the app.

The user may need to grant the OS permission for this particular app to install packages.

Provide a clear in-app message explaining this only when necessary.

Example:

```text
Update Required

To finish installing the update, Android may ask you
to allow this app to install updates.

[ CONTINUE ]
```

Do not send users to a website.

---

# POST-INSTALL VERIFICATION

This is extremely important.

Do not assume that calling the installer means the update succeeded.

After returning to the app:

1. Read the currently installed `versionCode`.
2. Compare it against `minimumSupportedVersionCode`.
3. If the version is sufficient:

   * Continue to welcome screen.
4. If not:

   * Remain blocked.
   * Show the update screen again.

Example:

```text
Installer launched
      ↓
User installs / cancels
      ↓
App resumes
      ↓
Read installed versionCode
      ↓
Version >= minimumSupportedVersionCode?
       / \
     YES  NO
      ↓    ↓
    App   Block + Retry
```

---

# HANDLE UPDATE INTERRUPTION

Handle:

* App sent to background
* App killed
* Device restarted
* Download interrupted
* Network lost
* APK corrupted
* User cancels installation
* User returns after installation
* Installer fails
* Server unreachable
* Invalid API response
* Invalid APK URL
* SHA-256 mismatch
* Update already downloaded
* Update download partially completed

Do not crash.

Mandatory updates cannot be bypassed.

---

# RETRY / ERROR STATES

Create proper states for:

```text
CHECKING_UPDATE
NO_UPDATE
UPDATE_REQUIRED
DOWNLOADING
VERIFYING
INSTALLING
UPDATE_SUCCESS
UPDATE_CANCELLED
UPDATE_FAILED
NETWORK_ERROR
INVALID_UPDATE
```

The UI should react to these states cleanly.

---

# UPDATE UI

Create a polished Material 3 update experience.

Mandatory update screen:

```text
──────────────────────────────

        UPDATE REQUIRED

A new version of the application
is required to continue.

Version 1.4.0

• Bug fixes
• Performance improvements
• Security improvements

        [ UPDATE NOW ]

──────────────────────────────
```

During download:

```text
──────────────────────────────

       DOWNLOADING UPDATE

             68%

       34.2 MB / 50 MB

     ███████████░░░░

──────────────────────────────
```

After an error:

```text
──────────────────────────────

         UPDATE FAILED

We couldn't complete the update.

Please check your connection
and try again.

          [ RETRY ]

──────────────────────────────
```

Design should feel like a modern production Android application.

Do NOT over-design it.

---

# BACK BUTTON / BYPASS PREVENTION

For a mandatory update:

* Back should not let the user escape to the welcome screen.
* No "Skip".
* No "Later".
* No "Continue without updating".
* No navigation around the update screen.
* Do not expose the application's main content behind the update UI.
* If the Activity is recreated, the mandatory-update state must be restored/re-evaluated.

Do not rely solely on hiding a button.

The underlying navigation/state architecture must prevent bypassing the requirement.

---

# OFFLINE BEHAVIOR

Think carefully about this.

If the app has already determined that a newer version is mandatory, it should remain blocked until it can obtain/install the required version.

If the update API itself cannot be reached, distinguish:

```text
Known mandatory update
```

from:

```text
Unable to check update server
```

Do not incorrectly treat a network failure as "up to date".

Use a sensible fail-closed/fail-safe strategy and document it.

For this private app, prioritize enforcing the mandatory version requirement.

---

# SECURITY

Do NOT blindly trust arbitrary APK URLs.

At minimum:

* HTTPS
* SHA-256 integrity verification
* Validate API data
* Never execute arbitrary downloaded files
* Use Android package installation mechanisms
* Do not store secrets in the APK
* Avoid hard-coded API credentials
* Keep update endpoint configurable

Also document an important security property:

**Android package updates must be signed with the same signing identity as the currently installed application.**

Do not design a system that assumes a random APK can replace the application.

---

# GITHUB ACTIONS BUILD SYSTEM

Use GitHub Actions to build the APK automatically.

I specifically want you to study this repository as a reference:

https://github.com/udaysharmadev/Maala-Counter

Look at how its GitHub Actions workflow builds the Android APK.

Its workflow uses a setup similar to:

```text
actions/checkout
actions/setup-node
actions/setup-java
android-actions/setup-android
sdkmanager
Gradle
actions/upload-artifact
```

Use the repository as an **implementation reference for the CI/build workflow**, but do NOT blindly copy unrelated code.

My project should have its own proper workflow.

---

# GITHUB ACTIONS WORKFLOW

Create:

```text
.github/workflows/android-build.yml
```

The workflow should:

1. Checkout repository.
2. Configure Java.
3. Configure Android SDK.
4. Install required Android platform/build tools.
5. Make Gradle executable.
6. Build the application.
7. Produce the APK.
8. Upload the generated APK as a GitHub Actions artifact.

Also support:

```text
workflow_dispatch
```

so I can manually trigger a build.

Build at least a debug APK initially.

Preferably structure the workflow so release builds can later be enabled without redesigning everything.

---

# RELEASE BUILD PREPARATION

Even though this is initially a simple private application, prepare the project for real release signing.

Explain how to configure:

```text
release keystore
key alias
keystore password
key password
```

using GitHub Actions secrets.

DO NOT commit the keystore or passwords into Git.

The release APK must always be signed consistently so Android accepts future updates as upgrades.

---

# IMPORTANT VERSIONING + CI REQUIREMENT

Make sure changing the app version is straightforward.

For example:

Version 1:

```text
versionCode = 1
versionName = "1.0.0"
```

Version 2:

```text
versionCode = 2
versionName = "1.1.0"
```

The GitHub Actions workflow should build the APK from the repository source.

The resulting APK should then be usable as the artifact that I distribute through my private update server.

---

# PRIVATE RELEASE FLOW

Design the overall workflow around:

```text
Developer
   ↓
Git push
   ↓
GitHub Actions
   ↓
Build signed APK
   ↓
APK artifact
   ↓
Upload/copy APK to private release server
   ↓
Update API points to latest APK
   ↓
Existing app checks API
   ↓
Existing app downloads update
   ↓
Android installation
```

The GitHub repository itself does NOT need to be the public APK distribution server.

The update API and APK server should be separate concerns.

---

# OPTIONAL FUTURE AUTOMATION

Structure the project so that later I can automate:

```text
GitHub push
    ↓
GitHub Actions build
    ↓
Create release APK
    ↓
Upload APK to server/storage
    ↓
Calculate SHA-256
    ↓
Update version metadata/API
```

For now, however, do not add unnecessary external infrastructure unless needed.

Document exactly where the automated release step would be added later.

---

# SERVER/API EXAMPLE

Create documentation for a simple backend endpoint.

For example:

```http
GET /api/app/update
```

Response:

```json
{
  "latestVersionCode": 2,
  "latestVersionName": "1.1.0",
  "minimumSupportedVersionCode": 2,
  "forceUpdate": true,
  "apkUrl": "https://updates.example.com/app/app-1.1.0.apk",
  "sha256": "abcdef...",
  "fileSize": 48234412,
  "message": "Please update to continue.",
  "releaseNotes": [
    "Bug fixes",
    "Performance improvements"
  ]
}
```

Explain exactly how I would change this later for my own backend.

---

# TESTING WITHOUT GOOGLE PLAY

I specifically do NOT want the testing instructions to depend on Google Play.

Explain how I can test this using private APK distribution.

For example:

### VERSION 1

Build and install:

```text
versionCode = 1
versionName = 1.0.0
```

### VERSION 2

Build:

```text
versionCode = 2
versionName = 1.1.0
```

Upload version 2 to my update server.

Set the API response:

```text
latestVersionCode = 2
minimumSupportedVersionCode = 2
forceUpdate = true
```

Launch version 1.

Expected:

```text
Version 1 launches
↓
API check
↓
Version 2 detected
↓
App becomes blocked
↓
Update screen appears
↓
APK downloads inside app
↓
SHA-256 verified
↓
Android installer opens
↓
User approves installation
↓
App resumes
↓
Version 2 detected
↓
Welcome screen appears
```

---

# TEST CASES

Implement and document at least these tests:

### Test 1 — Already latest

```text
Installed version = 2
Latest version = 2

→ Welcome screen
```

### Test 2 — Mandatory update

```text
Installed = 1
Latest = 2
Minimum = 2
Force = true

→ Block
→ Download
→ Install
→ Verify
```

### Test 3 — User cancels installer

```text
Installed = 1
Update required

→ Installer opens
→ User cancels

→ App returns
→ Still blocked
→ Retry
```

### Test 4 — Bad SHA-256

```text
Downloaded APK hash != API hash

→ Reject APK
→ Delete APK
→ Show error
→ Retry
```

### Test 5 — Offline server

```text
API unavailable

→ No false "up to date" result
→ Show appropriate error/state
```

### Test 6 — App killed during download

```text
Download interrupted

→ App restarts
→ Re-check update
→ Resume/retry appropriately
```

### Test 7 — App killed after installer opened

```text
App restarts
→ Check installed version
→ If update succeeded → allow app
→ Otherwise → mandatory update remains
```

### Test 8 — Invalid APK

Ensure a corrupt or incompatible APK never gets installed.

---

# SIMPLE CURRENT APPLICATION

Aside from the update system, the current app should only contain:

```text
Welcome 👋

Welcome to the App
```

Create a modern, clean welcome screen.

Do NOT add login systems, databases, complicated navigation, or unnecessary features.

The update system is the main project.

---

# PROJECT QUALITY

Before finishing:

1. Create all required files.
2. Build the application.
3. Run Gradle.
4. Fix all compilation errors.
5. Fix manifest issues.
6. Fix dependency issues.
7. Verify the APK is generated.
8. Verify the GitHub Actions workflow syntax.
9. Verify the update architecture is actually connected to the application startup flow.
10. Verify no fake/mock update implementation is being presented as real.

Do not stop at pseudocode.

---

# DOCUMENTATION

Create a comprehensive README explaining:

## Local development

How to:

```text
clone
open
sync
build
run
```

## Versioning

How to increment:

```text
versionCode
versionName
```

## GitHub Actions

How the APK is built.

## Release signing

How to configure signing secrets.

## Update API

Expected JSON response.

## Update server

Where the APK is hosted.

## Client update flow

How the Android app checks/downloads/installs/verifies updates.

## Testing

Exactly how to test:

```text
1.0.0 → 1.1.0
```

without Google Play.

---

# FINAL OUTPUT

At the end, report:

### 1. Technology stack

### 2. Project structure

### 3. How the forced update mechanism works

### 4. How APK downloading works

### 5. How APK integrity is verified

### 6. How Android installation is triggered

### 7. How bypassing mandatory updates is prevented

### 8. How GitHub Actions builds the APK

### 9. How I test Version 1 → Version 2 privately

### 10. Exactly what I need to change to connect my real update server

---

# CRITICAL INSTRUCTION

Do not use Google Play.

Do not implement a fake update popup.

Do not open Chrome.

Do not redirect to a website.

Do not depend on another downloader application.

Do not silently claim APK installation can happen without Android's permission/security flow.

Build a **real private APK update system** using my own update API/server, while keeping the download and update experience inside the application as much as Android permits.

Use the Maala-Counter repository's GitHub Actions build workflow as a reference for the CI/CD portion:

https://github.com/udaysharmadev/Maala-Counter
