# App scan workflow

The Android client now uses the unified app endpoints:

- `POST tasks/{taskId}/scans`
- `POST tasks/{taskId}/scans/{currentScanId}/corrections`
- `GET tasks/{taskId}/work-areas/{locationId}/floors`
- `GET tasks/{taskId}/floors/{floorId}/places`
- `GET tasks/{taskId}/work-areas/{branchId}/locations`
- `POST tasks/{taskId}/work-areas/{branchId}/locations/{locationId}/complete`

Each physical submission owns an immutable UUID. A network retry reuses the same
request object, UUID and image bytes. A correction always gets a new UUID and
targets `currentAcceptedScanId`.

## Scan user experience

- Vehicle scans stay on the same scanner screen. Successful results use a
  point-of-sale style overlay and close after 2.8 seconds. Mismatch, review,
  duplicate and correction-capable results remain until the employee closes
  them or starts the correction.
- `CONFLICT` never replaces the first accepted scan. When `correctionAllowed`
  is true, the employee may submit an explicit correction; otherwise the app
  explains that the conflict was recorded for supervisor review.
- Extra and ambiguous items are warning results and stay open until the
  employee closes the result dialog. They are never shown as auto-closing
  successes.
- Spare-part tasks open a searchable location list before the scanner. The
  backend `progressStatus` is the only source for grey, blue, green and orange
  state badges. In `BASIC` mode only not-started and in-progress are shown. In
  `DETAILED` mode the list also shows completed and review-required states, and
  offers completion only when the API returns `canComplete=true`.
- A spare-part location may be completed with differences. It remains visible
  as `REVIEW_REQUIRED`, and a later scan reopens it automatically.
- Spare-part input order is barcode, required sensor image (when configured),
  counted quantity, then verify. Quantity is never silently defaulted into a
  submitted request.
- Asset tasks open searchable floor and place lists before scanning. The chosen
  path is fixed on the scanner screen to prevent accidental location changes.
- The result overlay and the persistent last-result card display the safe item
  summary and actual scanned path returned by the backend. Expected stock,
  variance and expected location remain hidden.

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
