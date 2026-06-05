# Keep public API
-keep public class io.testpulse.sdk.TestPulse { *; }
-keep public class io.testpulse.sdk.TestPulseInitProvider { *; }

# Keep Moshi generated adapters
-keep class io.testpulse.sdk.model.** { *; }
-keepclassmembers class io.testpulse.sdk.model.** { *; }

# Keep Room entities
-keep class io.testpulse.sdk.db.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
