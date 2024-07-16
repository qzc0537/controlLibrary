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

-keep class com.rhizo.libcall.** { *; }

#webrtc
-keep class org.webrtc.** { *; }

# 保留Netty相关类和方法
-keepclassmembers class io.netty.** {
    *;
}
-keepclassmembers public class * extends io.netty.** {
    *;
}
-keepclassmembers class org.jboss.netty.** {
    *;
}
-keepclassmembers public class * extends org.jboss.netty.** {
    *;
}
-keep class io.netty.** { *; }
-keepattributes Signature,InnerClasses
-dontwarn io.netty.**
-dontwarn sun.**

# 保留所有继承自java.util.logging.Logger的类的getLogger()方法
-keepclassmembers class * extends java.util.logging.Logger {
    <init>(java.lang.String);
}

# 保留所有SPI(Service Provider Interface)的类
-keep class org.apache.** {
    <fields>;
    <methods>;
}
-keep class org.apache.log4j.** {
    <fields>;
    <methods>;
}
-keep class io.netty.util.internal.logging.** {
    <init>(...);
}
-keep class io.netty.util.internal.PlatformDependent {
    public static ** isAndroid();
}

# 如果你使用了Protobuf, 需要保留相关的Message类
-keep class * extends com.google.protobuf.GeneratedMessageLite {
    *;
}
-keepclassmembers class * {
    public static com.google.protobuf.Internal$ProtobufList asList(...);
}

# 如果你使用了UnixDomainSocket，需要保留相关的类
-keep class org.newsclub.net.unix.** {
    *;
}

-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.Level
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.logging.log4j.message.MessageFactory
-dontwarn org.apache.logging.log4j.spi.ExtendedLogger
-dontwarn org.apache.logging.log4j.spi.ExtendedLoggerWrapper
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.ILoggerFactory
-dontwarn org.slf4j.Logger
-dontwarn org.slf4j.LoggerFactory
-dontwarn org.slf4j.Marker
-dontwarn org.slf4j.helpers.FormattingTuple
-dontwarn org.slf4j.helpers.MessageFormatter
-dontwarn org.slf4j.helpers.NOPLoggerFactory
-dontwarn org.slf4j.spi.LocationAwareLogger