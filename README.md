<div align="center">

# 🫀 TestPulse

### Never get rejected by Google Play closed testing again.

[![JitPack](https://jitpack.io/v/Chakshu1221/testpulse-sdk.svg)](https://jitpack.io/#Chakshu1221/testpulse-sdk)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/minSdk-24-blue)](https://developer.android.com/about/versions/nougat)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**A drop-in Android SDK + Web Dashboard that tracks real tester engagement during Google Play's 14-day closed testing — so your production access request never gets rejected.**

[Dashboard](https://testpulse-dashboard-x3yd.onrender.com) · [Get API Key](#-getting-started) · [Integration](#-integration) · [API Reference](#-api-reference)

---

</div>

## 😫 The Problem

Google Play requires indie developers to run a **14-day closed test** with at least **20 testers** who are **genuinely engaged**. But here's what actually happens:

- ❌ Testers install once and **never come back**
- ❌ You have **zero visibility** into who's actually testing
- ❌ Google rejects your production access: *"Testers were not engaged"*
- ❌ You waste **another 14 days** restarting the process
- ❌ You beg strangers on Reddit/Discord who never follow through

**This cycle repeats for weeks.** Some developers give up entirely.

---

## ✅ The Solution

TestPulse gives you **full visibility** into your testers' real activity — with just **2 lines of code**.

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────────┐
│   Your App +    │──────▶│  TestPulse API   │──────▶│  TestPulse Dashboard│
│   TestPulse SDK │       │  (Auto-ingests)  │       │  (You monitor here) │
└─────────────────┘       └──────────────────┘       └─────────────────────┘
     Tracks:                   Stores:                    Shows:
  • Sessions                • All telemetry            • Per-tester activity
  • Screens                 • Engagement scores        • 14-day timeline
  • Duration                • Daily metrics            • Engagement score
  • Device info             • Alerts                   • Inactive alerts
```

---

## 🚀 Features

| Feature | Description |
|---|---|
| 📊 **Session Tracking** | Auto-detects app open/close via `ProcessLifecycleOwner` |
| 📱 **Screen Tracking** | Auto-tracks Activity transitions + manual `logScreen()` for Flutter |
| ⏱️ **Duration Metrics** | Per-session and per-screen time tracking |
| 👤 **Tester Registration** | First-launch dialog collects tester name for identification |
| 📴 **Offline Resilient** | Events queued in Room DB — syncs when online |
| 🔄 **Auto Batching** | Flushes every 60s + on app background — zero work for you |
| 🤖 **Zero Boilerplate** | Auto-initializes via `ContentProvider` — no `Application` class changes |
| 🪶 **Lightweight** | < 200KB AAR, minimal dependencies |
| 🗑️ **Easy Removal** | Remove 1 Gradle line + 1 manifest tag. Done. |
| 🌐 **Works Everywhere** | Native Android, Flutter, React Native, Jetpack Compose |

---

## 📋 Requirements

- Android **minSdk 24** (Android 7.0+)
- Kotlin 1.9+
- Gradle 8.x+

---

## 🔑 Getting Started

**1.** Open the **[TestPulse Dashboard](https://testpulse-dashboard-x3yd.onrender.com)** and create a free account

**2.** Create a project for your app

**3.** Copy your API key from the Settings page

**4.** Follow the integration guide below ⬇️

---

## 📦 Integration

### Native Android

<details open>
<summary><b>Step 1 — Add JitPack repository</b></summary>

In `settings.gradle.kts`:

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

<details open>
<summary><b>Step 2 — Add the dependency</b></summary>

In `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Chakshu1221:testpulse-sdk:1.0.+")
}
```

</details>

<details open>
<summary><b>Step 3 — Add your API key</b></summary>

In `AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="YOUR_API_KEY" />
</application>
```

</details>

**That's it. No init code. No `Application` class changes. Ship it.** ✅

---

### Flutter

<details open>
<summary><b>Step 1 — Add JitPack + dependency</b></summary>

In `android/app/build.gradle.kts` (**after** the `android { }` block):

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

</details>

<details open>
<summary><b>Step 2 — Add API key</b></summary>

In `android/app/src/main/AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="io.testpulse.API_KEY"
        android:value="YOUR_API_KEY" />
</application>
```

</details>

> ⚠️ **Flutter note:** Do **NOT** add JitPack in `settings.gradle.kts`. Flutter plugin subprojects cannot resolve it there. Always use `android/app/build.gradle.kts`. The meta-data goes in `android/app/src/main/AndroidManifest.xml` — not the root manifest.

---

### Auto-Init (Zero Boilerplate)

The SDK auto-initializes via a `ContentProvider` — no manual init code required. Internet and network state permissions are declared automatically.

If you prefer manual init, disable auto-init:

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

## ⚙️ How It Works

```
App Installed → Tester Registration Dialog → Tracking Begins
                                                    │
                    ┌───────────────────────────────┤
                    ▼                               ▼
             Session Tracking               Screen Tracking
          (foreground/background)        (Activity transitions)
                    │                               │
                    └───────────┬───────────────────┘
                                ▼
                     Local Room DB Queue
                                │
                     ┌──────────┴──────────┐
                     ▼                     ▼
              Every 60 seconds      On app background
                     │                     │
                     └──────────┬──────────┘
                                ▼
                   POST → TestPulse Backend
                                │
                                ▼
                     TestPulse Dashboard
                    (you monitor here 📊)
```

1. **First launch** → Registration dialog asks tester for their name
2. **Auto-tracking begins** → Sessions, screens, duration, device info
3. **Events queue locally** → Stored in Room DB (survives offline/crashes)
4. **Auto-flush** → Every 60 seconds + when app goes to background
5. **You monitor** → Watch real-time engagement on the [Dashboard](https://testpulse-dashboard-x3yd.onrender.com)

---

## 📖 API Reference

### `TestPulse.isInitialized: Boolean`

Check if the SDK initialized successfully. If `false`, all API calls are safe no-ops.

---

### `TestPulse.logEvent(name, data?)`

Log a custom event with optional metadata.

```kotlin
TestPulse.logEvent("purchase_flow_started")
TestPulse.logEvent("tutorial_completed", mapOf("step" to "5"))
```

---

### `TestPulse.logScreen(screenName)`

Manually log a screen view. Auto-tracking already covers Activities, but use this for **Flutter** or **single-Activity** apps.

```kotlin
TestPulse.logScreen("SettingsPage")
```

---

### `TestPulse.flush()`

Force-flush queued events to the server immediately.

```kotlin
TestPulse.flush()
```

---

### `TestPulse.setTesterAlias(alias)`

Update the tester's display name after registration.

```kotlin
TestPulse.setTesterAlias("John Doe")
```

---

## 🔍 Monitoring (Logcat)

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

## 🆚 Why TestPulse?

| | Without TestPulse | With TestPulse |
|---|---|---|
| **Tester visibility** | ❌ Blind — no idea who's testing | ✅ See every tester, every session |
| **Inactive testers** | ❌ Discover on Day 15 (too late) | ✅ Alerts on Day 2 — replace them |
| **Engagement proof** | ❌ Hope Google accepts your app | ✅ PDF report with real data |
| **Integration effort** | — | ✅ 2 lines of code |
| **Removal** | — | ✅ Delete 1 dependency + 1 tag |
| **Cost** | — | ✅ **Free** |

---

## ❓ FAQ

<details>
<summary><b>Does this work with Flutter apps?</b></summary>

**Yes!** Flutter Android apps are regular Android apps under the hood. The SDK auto-tracks sessions and app lifecycle. For per-screen tracking, use `TestPulse.logScreen("ScreenName")` since Flutter uses a single Activity.

</details>

<details>
<summary><b>Does this work with React Native / Compose?</b></summary>

**Yes!** Any Android app that uses Activities and has a standard Gradle build will work. The SDK auto-initializes via `ContentProvider`.

</details>

<details>
<summary><b>Will it slow down my app?</b></summary>

**No.** The SDK is < 200KB, uses background threads for all I/O, and batches network calls. Zero impact on UI thread.

</details>

<details>
<summary><b>What data is collected?</b></summary>

Only: tester alias (voluntarily given), session times, screen names, device model, OS version, screen resolution. **No PII, no email, no phone, no location, no contacts, no clipboard.**

</details>

<details>
<summary><b>How do I remove the SDK for production?</b></summary>

Use `debugImplementation` instead of `implementation` — the SDK is automatically excluded from release builds. Or simply remove the dependency and the manifest `<meta-data>` tag.

```kotlin
// Only included in debug builds
debugImplementation("com.github.Chakshu1221:testpulse-sdk:1.0.+")
```

</details>

<details>
<summary><b>Is this against Google Play policy?</b></summary>

**No.** The SDK collects only anonymous usage analytics with user consent (the registration dialog). This is standard practice — Firebase Analytics, Mixpanel, and Amplitude do the same thing.

</details>

---

## 🗺️ Roadmap

- [x] Android SDK (Native + Flutter + React Native)
- [x] Web Dashboard with engagement scores
- [x] PDF/CSV engagement report export
- [x] Tester activity alerts
- [ ] Flutter plugin wrapper (`pub.dev`)
- [ ] Slack/Discord webhook notifications
- [ ] Multi-project dashboard
- [ ] Team collaboration features

---

## 🤝 Contributing

Contributions are welcome! If you've experienced the Google Play closed testing nightmare, you know why this matters.

1. Fork the repo
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## ⭐ Star This Repo

If TestPulse saved you from the closed testing nightmare, **give it a star** ⭐ — it helps other indie devs find it!

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">

**Built with ❤️ by indie devs, for indie devs.**

[Dashboard](https://testpulse-dashboard-x3yd.onrender.com) · [Report Bug](https://github.com/Chakshu1221/testpulse-sdk/issues) · [Request Feature](https://github.com/Chakshu1221/testpulse-sdk/issues)

</div>
