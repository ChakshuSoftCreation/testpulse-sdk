# TestPulse Android SDK

[![](https://jitpack.io/v/ChakshuSoftCreation/testpulse-sdk.svg)](https://jitpack.io/#ChakshuSoftCreation/testpulse-sdk)

Track tester engagement during your **Google Play closed-testing** program. The SDK automatically collects session data, screen views, and custom events from your Android app and sends them to the TestPulse dashboard where you can monitor tester activity, engagement scores, and generate reports.

## Why TestPulse?

Google Play no longer approves production releases based on tester count alone. They now evaluate **tester engagement** — how actively testers use your app, how often they open it, which screens they visit, and whether they stick around for the full testing period.

Meeting the 12–14 tester minimum isn't enough. If testers install your app but never open it, or open it once and abandon it, Google may reject your production release.

TestPulse helps you **prove readiness** by showing:
- **Active testers** vs total installs — how many actually use the app
- **Session frequency** — are testers opening the app daily?
- **Screen depth** — are they exploring beyond the first screen?
- **Engagement trends** — is interest growing or dropping over the 14-day window?
- **At-risk testers** — who hasn't opened the app in 2+ days so you can nudge them

Use TestPulse to catch issues early, re-engage inactive testers, and ship to production with confidence.

## How It Works — End to End

```
1. Sign up on TestPulse dashboard  →  Create a project  →  Get API Key
2. Add this SDK to your app        →  Set API Key in AndroidManifest.xml
3. Users install your app          →  SDK auto-registers them as testers
4. Users test your app             →  SDK tracks sessions, screens, events
5. Data syncs to backend           →  View engagement on dashboard
```

## Prerequisites

- Android **minSdk 24** (Android 7.0) or higher
- A **TestPulse project** and **API key** (see step 1 below)

## Step 1: Get Your API Key from the Dashboard

1. Go to the **TestPulse dashboard** — [https://testpulse-dashboard-x3yd.onrender.com](https://testpulse-dashboard-x3yd.onrender.com)
2. **Sign up** for an account (or log in)
3. Click **"New Project"**, enter your app name and package name
4. The project is created and an **API key** is displayed — copy it
   ```
   tp_proj_6dd52f542dfcd1114e759bcfe97a266fcbbc6600240c8e13
   ```
5. You can always find your API key later under **Settings** → **API Keys** for any project

## Step 2: Add the SDK to Your App

### settings.gradle.kts

Add the JitPack repository:

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

### app/build.gradle.kts

Add the dependency:

```kotlin
dependencies {
    debugImplementation("com.github.ChakshuSoftCreation:testpulse-sdk:1.0.0")
}
```

> Use `debugImplementation` to track only in debug builds, or `implementation` to track in release builds too.

### Sync your project

Click **"Sync Now"** in Android Studio or run `./gradlew app:dependencies` from terminal.

## Step 3: Configure AndroidManifest.xml

Add two `<meta-data>` entries inside the `<application>` tag:

```xml
<application
    android:name=".MyApplication"
    android:icon="@mipmap/ic_launcher"
    ...>

    <!-- REQUIRED: Your API key from the dashboard -->
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="tp_proj_6dd52f542dfcd1114e759bcfe97a266fcbbc6600240c8e13" />

    <!-- OPTIONAL: Custom backend URL (defaults to https://testpulse-api-lfwq.onrender.com) -->
    <meta-data
        android:name="io.testpulse.BASE_URL"
        android:value="https://testpulse-api-lfwq.onrender.com" />

    <!-- Your existing activities -->
    <activity android:name=".MainActivity">
        ...
    </activity>
</application>
```

## Step 4: Initialize (Zero Code Required)

The SDK **auto-initializes** using a `ContentProvider` — there is no need to modify your `Application` class or call any init method.

If you prefer to initialize manually, call from your `Application.onCreate()` or `MainActivity.onCreate()`:

```kotlin
TestPulse.init(this)
```

That's it. The SDK will:
- Collect device info (model, OS, screen resolution, locale)
- Register the device as a tester with the backend
- Start tracking sessions automatically using lifecycle callbacks
- Queue events locally in Room DB and flush every 60 seconds

## Step 5: Track Custom Data (Optional)

### Record Screen Views

Call this in each Activity's `onResume()`:

```kotlin
override fun onResume() {
    super.onResume()
    TestPulse.recordScreenView("HomeScreen")
}
```

```kotlin
override fun onResume() {
    super.onResume()
    TestPulse.recordScreenView("CheckoutScreen")
}
```

### Record Custom Events

Track any in-app action with optional JSON data:

```kotlin
// Simple event
TestPulse.recordEvent("purchase_completed")

// Event with data
TestPulse.recordEvent(
    "level_up",
    mapOf("level" to 5, "score" to 1200, "character" to "warrior")
)
```

### Show Tester Registration Dialog

Prompt the tester to enter their name/alias:

```kotlin
TestPulse.showRegistrationDialog(activity) { alias ->
    // Optional: do something when tester registers
    Log.d("TestPulse", "Tester registered as: $alias")
}
```

The dialog looks like this:

```
┌──────────────────────────────┐
│     TestPulse Tester ID      │
│                              │
│  Enter your name or alias:   │
│  ┌──────────────────────┐   │
│  │                      │   │
│  └──────────────────────┘   │
│                              │
│        [  Submit  ]          │
└──────────────────────────────┘
```

It only shows once per device — subsequent calls are ignored.

## Full Example — Minimal Integration

**`app/build.gradle.kts`** (relevant parts):
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.3")
    debugImplementation("com.github.ChakshuSoftCreation:testpulse-sdk:1.0.0")
}
```

**`AndroidManifest.xml`**:
```xml
<application ...>
    <meta-data android:name="io.testpulse.API_KEY" android:value="tp_proj_..." />
</application>
```

**`MainActivity.kt`**:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TestPulse.showRegistrationDialog(this) {
            Toast.makeText(this, "Welcome, $it!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        TestPulse.recordScreenView("MainScreen")
    }

    fun onButtonClick() {
        TestPulse.recordEvent("button_clicked", mapOf("button" to "get_started"))
    }
}
```

## What Data Does the SDK Collect?

The SDK automatically collects device information to identify and profile testers:

| Data Point | Example |
|------------|---------|
| **Device model** | Pixel 7 |
| **Manufacturer** | Google |
| **OS version** | Android 14 |
| **SDK level** | 34 |
| **App version** | 1.0.0 (1) |
| **Screen resolution** | 1080x2400 |
| **Locale** | en_US |
| **Network type** | wifi |
| **Device UUID** | auto-generated unique ID |

Each session records:
- **Start and end time** (with duration)
- **Screens viewed** (name, enter/exit time, duration)
- **Custom events** (name, timestamp, optional JSON payload)

> No personally identifiable information is collected unless you explicitly pass it via custom events.

## API Reference

### TestPulse (public singleton)

| Method | Description |
|--------|-------------|
| `init(context)` | Initialize the SDK. Auto-called by ContentProvider — usually not needed. |
| `isInitialized` | Boolean — check if SDK is ready |
| `recordScreenView(screenName)` | Log a screen view with the given name |
| `recordEvent(name, data?)` | Log a custom event with optional `Map<String, Any?>` data |
| `startSession()` | Force-start a new session (auto-managed by lifecycle) |
| `endSession()` | Force-end current session |
| `getDeviceUuid(): String` | Get the unique device identifier |
| `showRegistrationDialog(activity, callback)` | Show alias input dialog (shows once per device) |
| `flush()` | Immediately send all queued events to the backend |
| `setBaseUrl(url)` | Override the backend URL at runtime |

### DeviceInfo (data class)

```kotlin
data class DeviceInfo(
    val deviceModel: String,
    val manufacturer: String,
    val osVersion: String,
    val sdkInt: Int,
    val appVersion: String,
    val appVersionCode: Int,
    val screenResolution: String?,
    val locale: String?,
    val networkType: String?
)
```

## How Data Flows

```
┌──────────────────────────────────────────────────────────────────┐
│                         Your Android App                          │
│                                                                   │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐  │
│  │ ScreenTracker │   │ EventTracker │   │   SessionTracker     │  │
│  │ recordScreen  │   │ recordEvent  │   │ lifecycle-aware      │  │
│  └──────┬───────┘   └──────┬───────┘   └──────────┬───────────┘  │
│         │                  │                       │              │
│         └──────────────────┼───────────────────────┘              │
│                            ▼                                      │
│                   ┌───────────────┐                               │
│                   │   DataBatcher  │  Buffers events locally       │
│                   │  (Room DB)     │  Flushes every 60 seconds    │
│                   └───────┬───────┘                               │
│                           │                                        │
│                           ▼                                        │
│                   ┌───────────────┐                               │
│                   │   ApiClient    │  POST /api/v1/ingest/         │
│                   │  (OkHttp)     │  with X-API-Key header        │
│                   └───────┬───────┘                               │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │   TestPulse Backend      │
              │   https://testpulse-api  │
              │   -lfwq.onrender.com     │
              └────────────┬────────────┘
                           │
                           ▼
              ┌─────────────────────────┐
              │  Supabase Database       │
              │  + Dashboard             │
              │  (engagement scores,     │
              │   reports, alerts)       │
              └─────────────────────────┘
```

## Troubleshooting

### "API key not found"
- Make sure you added `io.testpulse.API_KEY` to your `AndroidManifest.xml`
- Verify the key value matches the one shown in the dashboard Settings page

### "Tester not registered"
- The SDK registers the tester on first launch
- Check that your device has internet access
- Verify the `BASE_URL` is correct (default: `https://testpulse-api-lfwq.onrender.com`)

### "No data on dashboard"
- Data flushes every 60 seconds — wait up to 2 minutes
- Or call `TestPulse.flush()` to force-send
- Verify the API key in your manifest matches the project

### "Duplicate tester entries"
- The SDK deduplicates by `device_uuid` per project — each device registers once

## Build from Source

```bash
git clone https://github.com/ChakshuSoftCreation/testpulse-sdk.git
cd testpulse-sdk
./gradlew :testpulse-sdk:build
```

## Run Tests

```bash
./gradlew :testpulse-sdk:test
```

25 tests covering: API client, data batching, session tracking, screen tracking, and the full engagement flow.

## Publishing to JitPack

Every GitHub tag triggers a JitPack build:

```bash
git tag v1.0.0
git push origin v1.0.0
```

JitPack automatically builds the AAR and makes it available at `com.github.ChakshuSoftCreation:testpulse-sdk:VERSION`.

## License

MIT
