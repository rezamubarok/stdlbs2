# Support lib
-dontwarn android.support.v4.**
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

# Unity ads
# Keep all classes in Unity Ads package
# Keep filenames and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep JavascriptInterface for WebView bridge
-keepattributes JavascriptInterface

# Sometimes keepattributes is not enough to keep annotations
-keep class android.webkit.JavascriptInterface {
   *;
}

# Keep all classes in Unity Ads package
-keep class com.unity3d.ads.** {
   *;
}

# Keep all classes in Unity Services package
-keep class com.unity3d.services.** {
   *;
}

-dontwarn com.google.ar.core.**

# Admob
-keep public class com.google.firebase.analytics.FirebaseAnalytics {
    public *;
}

-keep public class com.google.android.gms.measurement.AppMeasurement {
    public *;
}

# End admob
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*

-keep @android.annotation.* public class *
-keep class com.bda.controller.** {
   *;
}

# Keep public native
-keep public class com.gamestudiolab.psgaming.player.NativeActivity { *; }
-keep public class com.gamestudiolab.psgaming.player.NativeApp { *; }
-keep public class com.gamestudiolab.psgaming.player.TextRenderer { *; }
-keep public class com.gamestudiolab.psgaming.player.CameraHelper { *; }
-keep public class com.gamestudiolab.psgaming.player.PpssppActivity { *; }
-keep public class com.gamestudiolab.psgaming.player.NativeSurfaceView { *; }
-keep public class com.gamestudiolab.psgaming.MainActivity {
    public java.lang.String pwd();
}

-keep public class com.gamestudiolab.psgaming.player.ShortcutActivity {
    public static native java.lang.String queryGameName(java.lang.String);
}