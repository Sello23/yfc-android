-ignorewarnings
-keepparameternames
-keepattributes *Annotation*

-keep class com.squareup.okhttp3.** {*;}

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-keep,allowobfuscation interface com.google.gson.annotations.SerializedName

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Android-Image-Cropper
-keep class androidx.appcompat.widget.** { *; }

# Rules for Kotlin Coroutines
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keepnames class androidx.navigation.fragment.NavHostFragment

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.internal.bind.** { *; }

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class kotlin.Metadata { *; }

# Retrofit
-keep class com.google.gson.** { *; }
-keep public class com.google.gson.** {public private protected *;}
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class retrofit.** { *; }
-keep class com.google.appengine.** { *; }
-dontwarn com.squareup.okhttp.*
-dontwarn rx.**
-dontwarn javax.xml.stream.**
-dontwarn com.google.appengine.**
-dontwarn java.nio.file.**
-dontwarn org.codehaus.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-if class *
-keepclasseswithmembers class <1> {
  <init>(...);
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

-keepattributes SourceFile,LineNumberTable

-renamesourcefileattribute SourceFile

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn com.google.android.libraries.places.api.Places
-dontwarn com.google.android.libraries.places.api.model.AddressComponent
-dontwarn com.google.android.libraries.places.api.model.AddressComponents
-dontwarn com.google.android.libraries.places.api.model.AutocompletePrediction
-dontwarn com.google.android.libraries.places.api.model.AutocompleteSessionToken
-dontwarn com.google.android.libraries.places.api.model.Place$Field
-dontwarn com.google.android.libraries.places.api.model.Place
-dontwarn com.google.android.libraries.places.api.model.TypeFilter
-dontwarn com.google.android.libraries.places.api.net.FetchPlaceRequest
-dontwarn com.google.android.libraries.places.api.net.FetchPlaceResponse
-dontwarn com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest$Builder
-dontwarn com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
-dontwarn com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
-dontwarn com.google.android.libraries.places.api.net.PlacesClient
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheet$CardScanResultCallback
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheet$Companion
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheet
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheetResult$Completed
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheetResult$Failed
-dontwarn com.stripe.android.stripecardscan.cardscan.CardScanSheetResult
-dontwarn com.stripe.android.stripecardscan.cardscan.exception.UnknownScanException
-dontwarn com.stripe.android.stripecardscan.payment.card.ScannedCard
-dontwarn javax.naming.NamingEnumeration
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.directory.DirContext
-dontwarn javax.naming.directory.InitialDirContext
-dontwarn javax.naming.directory.SearchControls
-dontwarn javax.naming.directory.SearchResult

-dontwarn androidx.databinding.**
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper

-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

-keep class com.spikeapi.** { *; }