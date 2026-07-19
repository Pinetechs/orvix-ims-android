# Orvix IMS Android - Technical Reference Document

هذا المستند مخصص للمبرمجين لشرح الهيكلية التقنية لتطبيق **Orvix IMS**، وكيفية صيانته أو تطويره مستقبلاً.

---

## 1. Architecture Overview (هيكلية النظام)
التطبيق يتبع نمط **MVVM (Model-View-ViewModel)** مع استخدام **Repository Pattern** لتوحيد مصادر البيانات.

- **UI (Activities/Adapters)**: مسؤولة فقط عن عرض البيانات وتفاعل المستخدم.
- **ViewModels**: تدير حالة الواجهة (UI State) وتتخاطب مع الـ Repositories.
- **Repositories**: الطبقة الوحيدة التي تتصل بالـ API (عبر Retrofit) وتدير العمليات المنطقية للبيانات.

---

## 2. Hardware Abstraction Layer - HAL (طبقة تجريد الهاردوير)
هذا هو الجزء الأهم في التطبيق، حيث تم بناؤه ليكون **Vendor-Neutral** (مستقلاً عن نوع الجهاز).

### المكونات الرئيسية:
- **`ScannerInterface`**: واجهة برمجية تحدد العمليات الأساسية (مسح، التقاط صورة، ضبط البروفايل). أي جهاز جديد يجب أن ينفذ هذه الواجهة.
- **`ScannerFactory`**: يستخدم نمط الـ Strategy لتحديد نوع الجهاز تلقائياً بناءً على الشركة المصنعة (`Build.MANUFACTURER`).
- **`ScannerProviderRegistry`**: السجل المركزي لجميع تعريفات الأجهزة المدعومة.

### دعم أجهزة UROVO:
- يتم التعامل مع أجهزة UROVO عبر **Intent Mode** (أداء أسرع وأكثر استقراراً).
- **`UrovoScannerManager`**: ينفذ واجهة المسح ويستمع لـ `android.intent.ACTION_DECODE_DATA`.
- **Image Capture**: يتم طلب الصورة بعد المسح مباشرة عبر `action.scanner_capture_image`.

---

## 3. Unified Scan Engine (محرك المسح الموحد)
لضمان تجربة مستخدم متناسقة في شاشات (المركبات، القطع، الأصول)، تم بناء مكونات مشتركة:

- **`ScanImageCoordinator`**: هو "المايسترو" الذي ينسق بين استلام الباركود وانتظار الصورة من الهاردوير. يقوم بقفل الزر الفعلي (Trigger) لمنع التداخل.
- **`ScanResultDialog`**: واجهة عرض النتائج الموحدة. تدعم الإغلاق التلقائي في حال النجاح، وتجبر المستخدم على التفاعل في حال وجود خطأ أو تضارب (Mismatch).
- **`ScanRequestFactory`**: يقوم ببناء كائنات الطلب (Multipart) لإرسال البيانات والصور للسيرفر بشكل موحد.

---

## 4. Localization & RTL (نظام التعريب)
- **`BaseActivity`**: كافة الأنشطة ترث من هذا الكلاس لضمان تطبيق اللغة والاتجاه (RTL) فورياً.
- **`LocaleHelper`**: يقوم بتغيير `Configuration.locale` وتحديث الموارد النصية.
- يتم تخزين خيار اللغة في `SharedPreferences` عبر `SessionManager`.

---

## 5. Security & Session Management (الأمن والجلسات)
- **`SessionManager`**: يدير بيانات العميل (API Base URL, Client Code) وبيانات المستخدم والتوكن.
- **`AuthInterceptor`**: يقوم بإضافة `Authorization: Bearer <token>` تلقائياً لكافة الطلبات الصادرة.
- **Secret Reset**: الضغط 10 مرات متتالية على شاشة الدخول يؤدي لإعادة ضبط تكوين التطبيق (لأغراض الصيانة).

---

## 6. Project Structure (ترتيب الملفات)
```text
com.pinetechs.orvix.ims.android
├── core/
│   ├── hardware/       # HAL (model, urovo, presentation)
│   ├── network/        # Retrofit config & Interceptors
│   ├── storage/        # SessionManager (SharedPrefs)
│   └── presentation/   # BaseActivity
├── auth/               # Login module
├── bootstrap/          # Client config & Updates
├── task/               # Task list module
├── workarea/           # Location hierarchy & Work areas
└── scan/               # Inventory scanning (Vehicle, SparePart, Asset)
```

---

## 7. Maintenance Tips (نصائح للصيانة)
1. **إضافة جهاز جديد**: أنشئ حزمة جديدة داخل `core.hardware` (مثلاً `zebra`) وانفذ `ScannerInterface` و `ScannerProvider` ثم سجلها في `ScannerProviderRegistry`.
2. **تعديل منطق المقارنة**: يتم ذلك داخل `messageFor` و `applyStatusStyle` في شاشات المسح المعنية.
3. **تحديث الـ API**: أضف الـ Endpoints الجديدة في الـ `Api` Interfaces المقابلة، وقم بتحديث الـ `Repository` المقابل.

---
**تم إعداد هذا المستند لضمان استمرارية العمل بأعلى جودة برمجية.**
