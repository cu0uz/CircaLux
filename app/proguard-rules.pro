# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/cu0uz/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard/index.html

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keep class com.google.gson.** { *; }
-keep class com.example.circalux.data.network.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepnames class kotlinx.serialization.json.Json { *; }

# CircaLux Data Models
-keep class com.example.circalux.data.model.** { *; }
