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

# Keep Gson / Retrofit models from being obfuscated in release build
-keepclassmembers class com.rohlicek.rateio.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses

# Keep all Kotlinx Serialization classes & Enums used in Navigation routes
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class com.rohlicek.rateio.model.** { *; }
-keep class com.rohlicek.rateio.navigation.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer();
}