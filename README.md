# TestPulse Android SDK

[![](https://jitpack.io/v/ChakshuSoftCreation/testpulse-sdk.svg)](https://jitpack.io/#ChakshuSoftCreation/testpulse-sdk)

Track tester engagement during Google Play closed testing — session frequency, screen flows, daily activity, and device info.

> **v1.0.6**: Daily task broadcasting + auto-crash capture. Polls server for day-specific tasks on session start, captures unhandled exceptions via `Thread.setDefaultUncaughtExceptionHandler`.

## Requirements

- Android **minSdk 24** (Android 7.0)
- Kotlin 1.9+
- Gradle 8.x+

## Integration

### Step 1: Add JitPack repository

<details>
<summary><b>Native Android project</b> (settings.gradle.kts)</summary>

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```
</details>

<details>
<summary><b>Flutter project</b> (android/app/build.gradle.kts)</summary>

Add **inside** your `android/app/build.gradle.kts`, after `android { }` block:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.ChakshuSoftCreation:testpulse-sdk:1.0.6")
}
```

> **Why not in settings.gradle.kts?** Flutter plugins declare their own repositories in subproject build files. Adding JitPack to settings-level `dependencyResolutionManagement` causes Gradle to search JitPack for Flutter internal artifacts, which breaks the build.
</details>

### Step 2: Add the dependency

```kotlin
dependencies {
    implementation("com.github.ChakshuSoftCreation:testpulse-sdk:1.0.6")
}
```

### Step 3: Add meta-data to AndroidManifest.xml

```xml
<application>
    <!-- Your existing components -->

    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="YOUR_API_KEY" />

    <!-- Optional: custom base URL (defaults to TestPulse cloud) -->
    <meta-data
        android:name="io.testpulse.BASE_URL"
        android:value="https://your-custom-server.com" />
</application>
```

### Step 4: Internet permission (already in SDK)

The SDK declares `INTERNET` and `ACCESS_NETWORK_STATE` permissions automatically — no action needed.

---

## How It Works

The SDK auto-initializes via a `ContentProvider` — no manual init code required.

1. On first launch, a registration dialog asks the tester for their name/alias
2. The tester is registered with the TestPulse backend
3. The SDK automatically tracks:
   - **Sessions** — app foreground/background via `ProcessLifecycleOwner`
   - **Screens** — `Activity` lifecycle (auto) + manual `logScreen()` calls
   - **Events** — via `logEvent()` API
   - **Device info** — model, OS version, screen size
4. Data is batched locally (Room DB) and flushed every 60 seconds

---

## API Reference

### `TestPulse.isInitialized: Boolean`

Whether the SDK initialized successfully. If `false`, all API calls are no-ops.

### `TestPulse.logEvent(name, data?)`

Log a custom event.

```kotlin
TestPulse.logEvent("purchase_flow_started")
TestPulse.logEvent("tutorial_completed", mapOf("step" to "5"))
```

### `TestPulse.logScreen(screenName)`

Manually log a screen view (auto-tracking already covers Activities).

```kotlin
TestPulse.logScreen("SettingsPage")
```

### `TestPulse.flush()`

Force-flush queued events to the server immediately.

```kotlin
TestPulse.flush()
```

### `TestPulse.setTesterAlias(alias)`

Update the tester's display name after registration.

```kotlin
TestPulse.setTesterAlias("John Doe")
```

---

## Monitoring (Logcat)

Filter by tag `TestPulse` to see SDK logs:

```bash
adb logcat -s TestPulse
```

| Message | Level | Meaning |
|---|---|---|
| `SDK initialized for project: ${key.take(12)}...` | ✅ `INFO` | Initialized successfully |
| `API key not found in AndroidManifest meta-data` | ❌ `WARN` | Missing or empty API key |
| `Failed to read meta-data` | ❌ `ERROR` | Exception reading manifest |
| `logEvent called before SDK initialization` | ⚠️ `WARN` | API called before init |
| `logScreen called before SDK initialization` | ⚠️ `WARN` | API called before init |

---

## Getting an API Key

1. Open the **[TestPulse Dashboard](https://testpulse-dashboard-x3yd.onrender.com)** and create an account
2. Create a project for your app
3. Copy the API key from the Settings page
4. Add it to your `AndroidManifest.xml` as shown above

---

## Sample Apps

- [Flutter integration example](https://github.com/Chakshu1221/TestPulse)
- [Native Android integration example](https://github.com/Chakshu1221/TestPulse)

---

## License

MIT
