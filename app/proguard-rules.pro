# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ruslandavletshin/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

-dontwarn okio.**

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

## Joda Time 2.3

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

##---------------Begin: proguard configuration for Retrofit 2  ----------
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
##---------------End: proguard configuration for Retrofit 2  ----------

# Picaso
-dontwarn com.squareup.okhttp.**

#Yandex metrica
-keep class com.yandex.metrica.impl.* { *; }
-dontwarn com.yandex.metrica.impl.*
-keep class com.yandex.metrica.* { *; }
-dontwarn com.yandex.metrica.*

# Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#Keep POJO
-keep class org.stepik.android.adaptive.data.model.** { *; }
-keep interface org.stepik.android.adaptive.data.model.** { *; }

-keep class org.stepik.android.adaptive.api.** { *; }
-keep interface org.stepik.android.adaptive.api.** { *; }
-dontwarn org.stepik.android.adaptive.api.**
-dontwarn org.stepik.android.adaptive.data.model.**

#Keep all enums
-keep public enum org.stepik.android.adaptive.**{
    *;
}

#keep configs names
-keep class org.stepik.android.adaptive.configuration.** { *; }
-keep interface org.stepik.android.adaptive.configuration.** { *; }
-dontwarn org.stepik.android.adaptive.configuration.**
