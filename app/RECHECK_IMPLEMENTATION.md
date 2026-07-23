# Android Recheck Requests

## User flow

1. The home screen opens `Recheck requests`.
2. The API returns only requests assigned to the signed-in employee.
3. A request contains one work area and only the items selected by the
   supervisor.
4. The employee starts the request, opens each pending item, and records one of:
   - found / counted;
   - not found;
   - unable to verify.
5. Found vehicles and assets require the requested barcode. Spare parts also
   require the counted quantity before scanning.
6. Evidence can come from the UROVO scanner sensor or a full-size device-camera
   photo. A required image blocks submission until it is present.
7. The employee submission becomes `SUBMITTED`. It never updates `currentScan`
   directly; the supervisor must approve it in Review Center.

## Code boundaries

- `recheck/data`: Retrofit API, repository, multipart creation, and DTOs.
- `recheck/presentation/list`: assigned request list.
- `recheck/presentation/detail`: request metadata, start action, and item list.
- `RecheckSubmissionActivity`: screen state and user interaction only.
- `RecheckSubmissionBuilder`: vehicle/asset/spare-part result rules.
- `RecheckLocationForm`: request-scoped floor/place/shelf controls.
- `RecheckScannerSession`: UROVO lifecycle and scan-image association.
- `RecheckCameraFile`: full-resolution temporary camera evidence.
- `RecheckUiText`: localized enum/status labels and date formatting.

The normal scan activities are not reused. Their task lifecycle and correction
rules are different from `UNDER_REVIEW`, and keeping the flows separate avoids
accidentally changing an accepted inventory result from the employee app.

## Required backend integration

The companion backend update:

- pre-populates expected branch/location/floor/place ids on every new recheck
  item;
- includes `expectedQuantity` in the item response;
- exposes asset floors/places and spare-part locations only within the assigned
  recheck, without exposing other task work areas.

No database migration is required for this integration update.
