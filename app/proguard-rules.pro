# ===================================================================
# FloraChain Android ProGuard & R8 Configuration
# ===================================================================

# --- Keep Domain and Data Models (Serialized with Gson/Room) ---
-keep class com.florachain.data.remote.dto.** { *; }
-keep class com.florachain.data.local.entity.** { *; }
-keep class com.florachain.domain.model.** { *; }

# --- Gson Serialized Fields ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Retrofit & OkHttp ---
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Web3j & Blockchain ---
-dontwarn org.web3j.**
-keep class com.florachain.data.blockchain.** { *; }

# --- Compose Runtime ---
-keep class androidx.compose.runtime.** { *; }

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- AndroidX Security & Crypto ---
-keep class androidx.security.crypto.** { *; }
