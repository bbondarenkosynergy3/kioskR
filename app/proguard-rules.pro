# Keep WebView JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView
-keep class android.webkit.** { *; }
-keepclassmembers class android.webkit.** { *; }

# Keep kiosk app classes
-keep class net.synergy360.kiosk.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
