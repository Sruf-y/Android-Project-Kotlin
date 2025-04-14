package StorageTest.Classes

import android.graphics.Bitmap
import java.io.File

data class InternalStoragePhoto (
    val name: String,
    val bitmap: Bitmap?,
    val file:File
)