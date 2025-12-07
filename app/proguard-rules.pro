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
#-dontshrink

-keep class com.android.langboarding.onboarding.strategy.** {
  *;
}
-keep class com.android.langboarding.language.strategy.** {
  *;
}
-keep class com.android.langboarding.language.adapter.** {
  *;
}


-keep class com.meg7.widget.** { *; }
-keep class javax.activation.** { *; }
-dontwarn javax.activation.**
-keep class org.apache.harmony.** { *; }
-dontwarn org.apache.harmony.**

-keep class com.google.common.cache.** { *; }
-dontwarn com.google.common.cache.**

-keep class com.google.common.primitives.** { *; }
-dontwarn com.google.common.primitives.**

-keep class okio.** { *; }
-dontwarn okio.**

-keep class com.avnsoft.photoeditor.photocollage.data.db.entity.** { *; }
-dontwarn com.avnsoft.photoeditor.photocollage.data.db.entity.**
-keep class com.avnsoft.photoeditor.photocollage.data.network.response.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.firebase.crash.**
-keep class com.firebase.** { *; }
-dontwarn java.awt.**
-dontwarn javax.security.**
-dontwarn java.beans.**
-dontwarn javax.xml.**
-dontwarn java.util.**
-dontwarn org.w3c.dom.**
-dontwarn com.google.common.**
-dontwarn oauth.signpost.jetty.**
-dontwarn org.mortbay.jetty.**
-dontwarn twitter4j.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.**
-dontwarn javax.management.**
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
-dontwarn com.actionbarsherlock.**
-dontwarn com.facebook.**
-dontwarn android.app.**
-dontwarn android.support.**
-dontwarn android.view.**
-dontwarn android.widget.**
-keep class com.android.vending.billing.**
-keep class io.paperdb.** { *; }
-keep class com.esotericsoftware.** { *; }
-dontwarn com.esotericsoftware.**
-keep class de.javakaffee.kryoserializers.** { *; }
-dontwarn de.javakaffee.kryoserializers.**

-keep class * implements java.io.Serializable { *; }

-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }

-dontwarn com.core.adslib.sdk.**
-keep class com.core.adslib.sdk.**{ *; }
#loggerSync
-dontwarn com.core.support.baselib.**
-keep class com.core.support.baselib.**{ *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#RenderScript
-keepclasseswithmembernames class * {
native <methods>;
}
-keep class androidx.renderscript.** { *; }
-keep class  com.amotech.photosdk.** { *; }
-keep class  org.wysaid.** { *; }

 -keep class com.appsflyer.** { *; }
 -keep public class com.miui.referrer.** {*;}
 -keep public class com.android.installreferrer.** { *; }
 -keep class kotlin.jvm.internal.Intrinsics{ *; }
 -keep class kotlin.collections.**{ *; }
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
    public *;
}
#-keep class com.avnsoft.photoeditor.photocollage.data.model.** { *; }
#-keep class com.android.langboarding.** {*;}
#-keep class com.android.langboarding.*$* {*;}
#-keep class com.android.langboarding.language.adapter.LanguageAdapter.** {
#    *;
#}
#-keep class com.android.langboarding.language.adapter.LanguageAdapter.*$* {
#    *;
#}