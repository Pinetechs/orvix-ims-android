# App scan workflow

The Android client now uses the unified app endpoints:

- `POST tasks/{taskId}/scans`
- `POST tasks/{taskId}/scans/{currentScanId}/corrections`
- `GET tasks/{taskId}/work-areas/{locationId}/floors`
- `GET tasks/{taskId}/floors/{floorId}/places`
- `GET tasks/{taskId}/work-areas/{branchId}/locations`

Each physical submission owns an immutable UUID. A network retry reuses the same
request object, UUID and image bytes. A correction always gets a new UUID and
targets `currentAcceptedScanId`.

## Device verification

1. Verify UROVO decode and capture-image broadcasts on the target firmware.
2. Verify that the physical trigger remains locked from decode until the API
   response, then becomes available again.
3. Test required-image tasks with valid, missing and timed-out sensor images.
4. Test first match, first mismatch, duplicate, different-location conflict,
   extra/review and explicit correction for every inventory domain.
5. Test a disconnected upload followed by **Retry upload** and confirm that the
   response reports an idempotent replay when the first request reached the server.
6. Confirm that spare-part screens never reveal expected stock, variance or the
   expected inventory location.
7. Open each scan screen and confirm that the UROVO profile is applied once. On
   firmware that rejects a CODE39 option, confirm that the log identifies the
   rejected option and that subsequent scan screens skip it without blocking
   the remaining CODE39 settings.

Release builds reject clear-text traffic. Configure the production bootstrap and
tenant API URLs with HTTPS before producing a release APK.
