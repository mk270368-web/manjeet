# Vidyut Gyan Kendra – Build & Deployment Guide

This guide provides instructions to build, run, test, and release the **Vidyut Gyan Kendra** educational management app.

---

## 1. Prerequisites

- **Android Studio:** Hedgehog (2023.1.1) or newer / Ladybug
- **JDK:** OpenJDK 17 or 21
- **Android SDK:** Compile SDK 36 (Android 15), Min SDK 24 (Android 7.0+)
- **Build System:** Gradle (Kotlin DSL) with Android Gradle Plugin 9.1.x and Jetpack Compose compiler

---

## 2. Opening & Running in Android Studio

1. Open Android Studio.
2. Select **File > Open** and choose the project root folder.
3. Allow Gradle sync to complete and download all dependencies.
4. Select a virtual device (Pixel 7 / 8 running Android 14 or 15) or connect a physical Android device with USB Debugging enabled.
5. Click the green **Run ▶** button (or press `Shift + F10`).
6. The app will launch with the **Vidyut Gyan Kendra** splash screen and open the **Welcome & Role Selection Portal**.

---

## 3. Testing Pre-Configured Accounts

The application automatically initializes seed cohorts and credentials on first launch:

- **Admin Login:**
  - Login ID: `ADM001`
  - Password: `admin@123`
  - Features: Create student/teacher accounts, manage classes & subjects, view attendance reports, export CSV, and check security audit logs.
- **Teacher Login:**
  - Login ID: `TCH101`
  - Password: `pass123`
  - Features: Mark attendance with Present/Absent/Late toggle and duplicate check, create and publish assignments, grade student submissions, share study notes.
- **Student Login:**
  - Login ID: `STU202401`
  - Password: `pass123`
  - Features: Profile details, subject-wise attendance %, low attendance alert (<75%), submit assignment solutions, view weekly timetable and teacher contacts.

*Note: Self-registration is strictly disabled by architectural policy. Only the Admin can create new user credentials.*

---

## 4. Running Automated Tests

Run the local JVM unit and Robolectric tests via Gradle:

```bash
gradle :app:testDebugUnitTest
```

---

## 5. Generating a Signed Release APK / Android App Bundle (AAB)

To prepare the application for Google Play Store publication:

### Option A: Via Android Studio UI
1. Go to **Build > Generate Signed Bundle / APK...**
2. Choose **Android App Bundle (.aab)** for Google Play Store, or **APK** for direct distribution.
3. Select your institutional upload keystore (`my-upload-key.jks`), enter the key alias and passwords.
4. Select the **release** build variant.
5. Click **Finish**. The output `.aab` or `.apk` will be generated in `app/release/`.

### Option B: Via Command Line
Ensure your signing credentials are configured in your environment or `app/build.gradle.kts`:

```bash
gradle :app:bundleRelease
```
or for an installable release APK:
```bash
gradle :app:assembleRelease
```

---

## 6. Configuring Cloud Firebase Backend (Optional / Production Migration)

1. Create a Firebase Project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.aistudio.vidyutgyankendra.rxqtpl`.
3. Download `google-services.json` and place it in the `app/` directory.
4. Deploy security rules from `firestore.rules`:
   ```bash
   firebase deploy --only firestore:rules
   ```
5. Enable **Email/Password** or institutional identity providers in Firebase Authentication.
