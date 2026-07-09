# Orvix IMS Android - Bootstrap Enabled

This Android Java/XML module was updated to support client bootstrapping.

## Added flow

1. `MainActivity` checks if client configuration exists.
2. If missing, opens `SetupActivity`.
3. `SetupActivity` supports two setup modes:
   - Client Code via Orvix Bootstrap Service
   - Manual Backend URL via client backend `/api/app/public/system-info`
4. The app stores client configuration in `SessionManager`.
5. `ApiClient` uses the stored `apiBaseUrl` dynamically for login/tasks/scan APIs.
6. Version check supports:
   - force update
   - optional update
   - APK URL opening for private distribution

## Expected Bootstrap endpoint

`GET /api/mobile/bootstrap/{clientCode}?platform=ANDROID&versionCode=1`

Direct response or ApiResponse wrapper are both supported.

Direct response example:

```json
{
  "clientCode": "BUSTAMI",
  "clientName": "Bustami & Al Saheb",
  "apiBaseUrl": "https://ims.bustami.com/",
  "logoUrl": "https://connect.orvix.com/logos/bustami.png",
  "active": true,
  "minSupportedAndroidVersionCode": 1,
  "latestAndroidVersionCode": 2,
  "forceUpdate": false,
  "androidApkUrl": "https://connect.orvix.com/downloads/orvix-ims-v2.apk",
  "releaseNotes": "Bug fixes and scanner improvements"
}
```

Wrapper response example:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "clientCode": "BUSTAMI",
    "clientName": "Bustami & Al Saheb",
    "apiBaseUrl": "https://ims.bustami.com/",
    "minSupportedAndroidVersionCode": 1,
    "latestAndroidVersionCode": 2,
    "forceUpdate": false,
    "androidApkUrl": "https://connect.orvix.com/downloads/orvix-ims-v2.apk"
  }
}
```

## Expected Manual Backend endpoint

`GET /api/app/public/system-info?platform=ANDROID&versionCode=1`

If `apiBaseUrl` is missing from this endpoint response, the entered backend URL is used as fallback.

## Important config

Change this value before real deployment:

```java
Constants.BOOTSTRAP_BASE_URL
```

Current default:

```java
http://10.0.2.2:8081/
```

For a real Android device or UROVO device, use the server LAN IP or public URL.

## Modified / added files

- `MainActivity.java`
- `core/network/ApiClient.java`
- `core/storage/SessionManager.java`
- `core/util/Constants.java`
- `core/util/VersionUtils.java`
- `bootstrap/data/BootstrapApi.java`
- `bootstrap/data/BootstrapRepository.java`
- `bootstrap/data/dto/BootstrapResponse.java`
- `bootstrap/presentation/SetupActivity.java`
- `bootstrap/presentation/SetupViewModel.java`
- `bootstrap/presentation/UpdateRequiredActivity.java`
- `res/layout/activity_setup.xml`
- `res/layout/activity_update_required.xml`
- `AndroidManifest.xml`
