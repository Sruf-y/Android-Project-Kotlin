package GlobalValues

object System {
    val RequiredPermissions = listOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_AUDIO,
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.CAMERA)
}