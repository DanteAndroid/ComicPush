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
## bugtags
  -keepattributes LineNumberTable,SourceFile
  -keep class com.bugtags.library.** {*;}
  -dontwarn com.bugtags.library.**
  -keep class io.bugtags.** {*;}
  -dontwarn io.bugtags.**
  -dontwarn org.apache.http.**
  -dontwarn android.net.http.AndroidHttpClient
  # End Bugtags
  -dontoptimize

  # jpush
  -dontpreverify
  -dontwarn cn.jpush.**
  -keep class cn.jpush.** { *; }
  -dontwarn cn.jiguang.**
  -keep class cn.jiguang.** { *; }
  # End jpush

    -keep class com.alibaba.** {*;}
    -dontwarn com.alibaba.**
    -keep class com.avos.**{*;}
    -dontwarn com.avos.**

#For design support library
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-ignorewarnings