# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.rhizo.libtcp.bean.** { *; }

# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

# 保持WebSocket相关的类不被混淆
-keep public class org.java_websocket.** {
    public *;
}
-keep public class org.eclipse.jetty.websocket.** {
    public *;
}

# 如果你使用了OkHttp和WebSocket的集成，可能还需要保持这些类
-keep public class okhttp3.websocket.** {
    public *;
}