# Orvix IMS Android - Initial Java/XML Project

Initial Android Native project for Orvix IMS inventory scanning.

## Stack

- Java
- XML Views
- MVVM
- Retrofit + OkHttp
- ViewModel + LiveData
- SharedPreferences session storage

## How to open

1. Open Android Studio.
2. Choose **Open**.
3. Select this folder: `orvix-ims-android`.
4. Wait for Gradle Sync.
5. Update `Constants.BASE_URL` depending on your test device.

## Backend URL

For Android Emulator:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/";
```

For a real Android device/UROVO on the same network:

```java
public static final String BASE_URL = "http://YOUR_PC_IP:8080/";
```

## Initial Flow

MainActivity -> LoginActivity -> TaskListActivity -> LocationSelectionActivity -> ScanActivity

## Notes

The API paths are placeholders aligned with the proposed app API:

- POST `/api/app/auth/login`
- GET `/api/app/inventory/tasks`
- GET `/api/app/inventory/tasks/{taskId}/locations`
- POST `/api/app/inventory/tasks/{taskId}/scan`

Adjust them if your backend endpoints are different.
