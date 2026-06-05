# TestPulse Android SDK

[![](https://jitpack.io/v/ChakshuSoftCreation/testpulse-sdk.svg)](https://jitpack.io/#ChakshuSoftCreation/testpulse-sdk)

TestPulse SDK tracks tester engagement during Google Play closed-testing programs. It automatically collects session data, screen views, and custom events from your Android app and sends them to the TestPulse backend for analysis.

## Features

- **Automatic initialization** via `ContentProvider` — no `Application` class changes needed
- **Session tracking** — start/stop sessions with lifecycle-aware auto-detection
- **Screen tracking** — log screen transitions with timestamps
- **Custom events** — track any in-app event with optional metadata
- **Offline queue** — stores unsynced events in Room DB, flushes every 60 seconds
- **Device profiling** — collects device model, OS version, locale, screen resolution
- **Tester registration** — automatically registers the device with the TestPulse backend

## Integration

### 1. Add the dependency

**`settings.gradle.kts`**
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

**`app/build.gradle.kts`**
```kotlin
dependencies {
    debugImplementation("com.github.ChakshuSoftCreation:testpulse-sdk:1.0.0")
}
```

> Use `debugImplementation` if you only want tracking in debug builds, or `implementation` for release builds too.

### 2. Configure in AndroidManifest.xml

```xml
<application ...>
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="tp_proj_YOUR_PROJECT_API_KEY" />

    <!-- Optional: defaults to https://testpulse-api-lfwq.onrender.com -->
    <meta-data
        android:name="io.testpulse.BASE_URL"
        android:value="https://your-custom-backend.com" />
</application>
```

### 3. Initialize

The SDK auto-initializes via `TestPulseInitProvider` — no code required.

To initialize explicitly (e.g. from `Application.onCreate()` or `MainActivity.onCreate()`):

```kotlin
TestPulse.init(this)
```

To display the tester registration dialog (alias input):

```kotlin
TestPulse.showRegistrationDialog(activity) { alias ->
    // Tester registered with alias
}
```

## API Reference

### TestPulse (singleton)

| Method | Description |
|--------|-------------|
| `init(context)` | Initialize SDK (auto-called by ContentProvider) |
| `isInitialized` | Check if SDK is initialized |
| `recordScreenView(screenName)` | Log a screen transition |
| `recordEvent(name, data?)` | Log a custom event with optional JSON data |
| `startSession()` | Manually start a session |
| `endSession()` | Manually end the current session |
| `getDeviceUuid(): String` | Get the unique device identifier |
| `showRegistrationDialog(activity, callback)` | Show alias input dialog |
| `flush()` | Force-flush pending events to backend |
| `setBaseUrl(url)` | Override the backend URL at runtime |

### Lifecycle

The SDK uses `ProcessLifecycleOwner` to automatically start a session when the app comes to the foreground and end it when it goes to the background. No manual session management is needed for most use cases.

## Data Collected

### Device Info
| Field | Example |
|-------|---------|
| deviceModel | Pixel 7 |
| osVersion | Android 14 |
| appVersion | 1.0.0 |
| screenResolution | 1080x2400 |
| locale | en_US |

### Session Data
| Field | Description |
|-------|-------------|
| sessionUuid | Unique session identifier |
| startTime | Session start timestamp |
| endTime | Session end timestamp |
| durationSec | Session duration in seconds |
| screens[] | Array of screen events viewed |
| events[] | Array of custom events recorded |

### Screen Event
| Field | Description |
|-------|-------------|
| screenName | Screen/activity name |
| enteredAt | Time entered |
| exitedAt | Time exited |
| durationSec | Time spent on screen |

## Build

```bash
./gradlew :testpulse-sdk:build
```

## Test

```bash
./gradlew :testpulse-sdk:test
```

Tests cover: API client, data batcher, session tracking, screen tracking, and full engagement flow (25 tests).

## Architecture

```
App ──▶ TestPulse.init()
            │
            ├──▶ DeviceCollector   →  device info (model, OS, etc.)
            ├──▶ SessionTracker    →  lifecycle-aware sessions
            ├──▶ ScreenTracker     →  Activity screen transitions
            ├──▶ EventTracker      →  custom in-app events
            ├──▶ TesterRegistration → register device with backend
            │
            ├──▶ DataBatcher       →  queue + flush (every 60s)
            ├──▶ ApiClient         →  HTTP calls to backend
            │
            └──▶ Room DB           →  offline storage
                        (EventDao / EventEntity)
```

## Publishing

The SDK is published to **JitPack**. Every GitHub tag triggers a new build:

1. Tag a release: `git tag v1.0.0 && git push origin v1.0.0`
2. JitPack builds the AAR automatically
3. Users reference `com.github.ChakshuSoftCreation:testpulse-sdk:VERSION`

## License

MIT
