# Keep application class and entry points
-keep class com.expense.smsextractor.Application { *; }
-keep class com.expense.smsextractor.MainActivity { *; }

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keepclassmembers class * {
    @androidx.room.* *;
}

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Material Components
-keep class com.google.android.material.** { *; }
-keep public class com.google.android.material.R$* { *; }

# Keep Kotlin metadata
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep view bindings
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static * bind(android.view.View);
    public static * inflate(android.view.LayoutInflater);
}

# Keep Room generated DAO classes
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep Paging components
-keep class androidx.paging.** { *; }

# Keep LiveData
-keep class androidx.lifecycle.LiveData
-keep class androidx.lifecycle.MutableLiveData
-keep class androidx.lifecycle.**$LifecycleEventObserver
