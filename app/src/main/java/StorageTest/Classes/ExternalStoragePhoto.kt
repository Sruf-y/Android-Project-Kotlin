package StorageTest.Classes

import android.net.Uri

data class ExternalStoragePhoto(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val contentUri: Uri
)