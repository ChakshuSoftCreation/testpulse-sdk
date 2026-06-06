# TestPulse Android SDK

[![](https://jitpack.io/v/Chakshu1221/testpulse-sdk.svg)](https://jitpack.io/#Chakshu1221/testpulse-sdk)

Track tester engagement during Google Play closed testing — session frequency, screen flows, daily activity, device info, and crash capture.

## Requirements

- Android **minSdk 24** (Android 7.0)
- Kotlin 1.9+
- Gradle 8.x+

## Integration

### Native Android

**Step 1** — Add JitPack in `settings.gradle.kts`:

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

**Step 2** — Add the dependency in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Chakshu1221:testpulse-sdk:1.0.+")
}
```

**Step 3** — Add API key to `AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="YOUR_API_KEY" />
</application>
```

### Flutter

**Step 1** — Add JitPack and the dependency in `android/app/build.gradle.kts` (after the `android { }` block):

```kotlin
android {
    // ... your existing config
}

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Chakshu1221:testpulse-sdk:1.0.+")
}
```

**Step 2** — Add API key to `android/app/src/main/AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="YOUR_API_KEY" />
</application>
```

> **Flutter note:** Do NOT add JitPack in `settings.gradle.kts`. Flutter plugin subprojects cannot resolve it there. Always use `android/app/build.gradle.kts`. The meta-data goes in `android/app/src/main/AndroidManifest.xml` — not the root `android/AndroidManifest.xml`.

### Auto-init (zero boilerplate)

The SDK auto-initializes via a `ContentProvider` — no manual init code required. Internet and network state permissions are declared automatically.

If you prefer manual init, add this to your manifest:

```xml
<meta-data android:name="io.testpulse.AUTO_INIT" android:value="false" />
```

Then initialize in `Application.onCreate()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TestPulse.initialize(this)
    }
}
```

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
