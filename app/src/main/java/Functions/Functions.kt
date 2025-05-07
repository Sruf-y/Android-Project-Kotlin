package Functions

import DataClasses_Ojects.Logs
import SongsMain.Classes.ModifiedItem
import SongsMain.Classes.TypeOfUpdate
import SongsMain.SongMain_Activity
import SongsMain.Tutorial.Application
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.ContentResolver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.animation.AccelerateInterpolator
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.Interpolator
import androidx.core.animation.LinearInterpolator
import androidx.core.animation.ValueAnimator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.Insets
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.composepls.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


// example of recyclerview configuration to add in order to be able tomove items around
//
//
//override fun onViewCreated(.............){
//    val itemTouchHelper= ItemTouchHelper(CardMovementCallback)
//    itemTouchHelper.attachToRecyclerView(recycleview)
//}
//
//private val CardMovementCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),0){
//    override fun onMove(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//        val startPosition = viewHolder.adapterPosition
//        val endPosition = target.adapterPosition
//        Collections.swap(alarmDataList,startPosition,endPosition)
//        recycleview.adapter?.notifyItemMoved(startPosition,endPosition)
//        return true
//    }
//
//    override fun onSwiped(
//        viewHolder: RecyclerView.ViewHolder,
//        direction: Int
//    ) {
//        TODO("Not yet implemented")
//    }
//
//}

/**
 * Requires the view to NOT be aware of the keyboard beforehand. Only other system insets
 * */
fun setAnimationForKeyboard(view: View,initialTranslationY:Float = 0f,typeMask:Int=WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()) {
    ViewCompat.setWindowInsetsAnimationCallback(
        view,
        object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
            var initialBottomInset = 0

            var offset: Float = 0f


            override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                super.onPrepare(animation)

                initialBottomInset = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0

            }

            override fun onStart(
                animation: WindowInsetsAnimationCompat,
                bounds: WindowInsetsAnimationCompat.BoundsCompat
            ): WindowInsetsAnimationCompat.BoundsCompat {



                return super.onStart(animation, bounds)
            }



            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: List<WindowInsetsAnimationCompat?>
            ): WindowInsetsCompat {
                var currentImeBottom =
                    insets.getInsets(WindowInsetsCompat.Type.ime()).toPlatformInsets().bottom


                val progress = runningAnimations.firstOrNull()?.interpolatedFraction ?: 0f

                val systembarsBottom =
                    insets.getInsets(typeMask).bottom



                if (currentImeBottom < initialBottomInset) {
                    // keyboard going down

                    //                                                                 inversedly map the progress for going down
                    val auxoffset = -currentImeBottom.toFloat() + (systembarsBottom * Functions.map(progress.toFloat(), 0f, 1f, 1f, 0f))

                    // apply the initial translationY as given by the parameter
                    offset = auxoffset+initialTranslationY


                } else if(currentImeBottom > initialBottomInset) {
                    //keyboard going up

                    val auxoffset = -currentImeBottom.toFloat() + (systembarsBottom * progress)

                    offset = auxoffset + initialTranslationY
                }


                // set the translation of the view, it will return to the initial one upon the keyboard's closing
                view.translationY = offset



                return insets
            }

            override fun onEnd(animation: WindowInsetsAnimationCompat) {
                super.onEnd(animation)

                // NO OP

                // Do not reset translation, as it will result in the view snapping back down (if ime insets were not accounted for, which should always be the case when using this)

                //view.translationY = offset
            }
        }
    )
}

class Extensions{
    companion object{

        val File.isCorrupted: Boolean
            get(){
                return try{
                    if(!this.exists() || this.length() == 0L)
                    {
                        throw IOException("File was not created or is empty")
                        true
                    }
                    else
                        false
                }catch (e: IOException){
                    throw IOException("File was not created or is empty")
                    true
                }
            }
    }
}


object Images{


    fun returnFixBitmapRotation(bitmap:Bitmap,bitmapFile:File): Bitmap? {

        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(bitmapFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientation = exif!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        var matrix: Matrix = Matrix();
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL ->
                return bitmap;

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                matrix.setScale(-1F, 1F);

            ExifInterface.ORIENTATION_ROTATE_180 ->
                matrix.setRotate(180F);

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180F);
                matrix.postScale(-1F, 1F);
            }

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90F);
                matrix.postScale(-1F, 1F);
            }

            ExifInterface.ORIENTATION_ROTATE_90 ->
                matrix.setRotate(90F);

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90F);
                matrix.postScale(-1F, 1F);
            }

            ExifInterface.ORIENTATION_ROTATE_270 ->
                matrix.setRotate(-90F);

            else ->
                return bitmap;
        }
        try {
            val bmRotated:Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch ( e:OutOfMemoryError) {
            e.printStackTrace();
            return bitmap;
        }

    }

    fun saveToFile(filename: String,bitmap: Bitmap?,toDirectory:File): Boolean {

        // Early return if bitmap is null
        if (bitmap == null) {
            Log.e("PhotoSave", "Cannot save null bitmap")
            return false
        }

        var directory: File = toDirectory


        if (!toDirectory.exists()) {
            directory.mkdirs()
        }


        val fullFilename = filename.replace(".jpg", "") + ".jpg"
        val file = File(directory, fullFilename) // Use proper context path
        val newbitmap = bitmap
        return try {
            if(file.exists()){
                file.delete()
            }
            // Use auto-closing stream
            FileOutputStream(file).use { outputStream ->


                if (!newbitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)) {
                    throw IOException("Failed to compress bitmap")
                }
            }

            true
        } catch (e: IOException) {
            // Clean up if something went wrong
            if (file.exists()) {
                file.delete()
            }
            Log.e("PhotoSave", "Error saving photo", e)
            false
        }
    }

    suspend fun loadFromFile(fila: File?): Bitmap? {
        var file = fila

        if (file != null)
            return withContext(Dispatchers.IO) {

                // daca nu are extensie pus in nume, ceea ce e o eroare a mea deasa, pune default ca .jpg
                if (!file.path.contains(".")) {
                    file = File(file.path + ".jpg")
                    Log.i(
                        Logs.MEDIA_IMAGES.name,
                        "Images.loadFromFile was given a extensionless filename. Assigning .jpg as default. Result: ${file.path}"
                    )
                }

                if (file.exists()) {
                    Log.i(Logs.MEDIA_IMAGES.name, "Images.loadFromFile a file that exists")
                    try {
                        if (file.length() <= 0)
                            file.delete()
                        val bytes = file.readBytes()

                        if (bytes.size <= 0)
                            file.delete()


                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {

                            try {
                                Images.returnFixBitmapRotation(
                                    bitmap,
                                    file
                                ) // the result is returned
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                bitmap
                            }
                        } else {
                            // Handle decode failure
                            Log.e(Logs.MEDIA_IMAGES.name, "Could not decode image: ${file.name}")
                            file.delete()
                            null // mapNotNull will filter this out
                        }
                    } catch (e: Exception) {
                        // Handle potential exceptions like OutOfMemoryError
                        Log.e(Logs.MEDIA_IMAGES.name, "Error processing image: ${file.name}", e)
                        file.delete()
                        null // mapNotNull will filter this out
                    }
                } else {
                    Log.i(Logs.MEDIA_IMAGES.name, "Images.loadFromFile a file that does NOT exist")
                    null
                }
            }
        else
            return null
    }

    fun loadFromFileinSync(fila:File?):Bitmap?{
        var file = fila

        if (file != null) {
            // daca nu are extensie pus in nume, ceea ce e o eroare a mea deasa, pune default ca .jpg
            if (!file.path.contains(".")) {
                file = File(file.path + ".jpg")
                Log.i(
                    Logs.MEDIA_IMAGES.name,
                    "Images.loadFromFile was given a extensionless filename. Assigning .jpg as default. Result: ${file.path}"
                )
            }

            if (file.exists()) {
                Log.i(Logs.MEDIA_IMAGES.name, "Images.loadFromFile a file that exists")
                try {
                    if (file.length() <= 0)
                        file.delete()
                    val bytes = file.readBytes()

                    if (bytes.size <= 0)
                        file.delete()


                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {

                        try {
                            return Images.returnFixBitmapRotation(
                                bitmap,
                                file
                            ) // the result is returned
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            return bitmap
                        }
                    } else {
                        // Handle decode failure
                        Log.e(Logs.MEDIA_IMAGES.name, "Could not decode image: ${file.name}")
                        file.delete()
                        return null // mapNotNull will filter this out
                    }
                } catch (e: Exception) {
                    // Handle potential exceptions like OutOfMemoryError
                    Log.e(Logs.MEDIA_IMAGES.name, "Error processing image: ${file.name}", e)
                    file.delete()
                    return null // mapNotNull will filter this out
                }
            } else {
                Log.i(Logs.MEDIA_IMAGES.name, "Images.loadFromFile a file that does NOT exist")
                return null
            }
        }
        else
            return null
    }

    fun getBitmapFromDrawable(drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(Application.instance, drawableResId)!!
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    fun loadFromUri(context: Context, uri: Uri): File? {
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            // This is a file URI, so you can directly convert it to a File.
            return File(uri.path!!)
        }
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
            // Unrecognized URI scheme.
            return null
        }
        val contentResolver = context.contentResolver

        // Query the content resolver for the file's display name.
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(nameIndex == -1) return null
                val displayName = it.getString(nameIndex)
                val tempFile = File.createTempFile("temp_", displayName, context.cacheDir)
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    return tempFile
                } catch (e: Exception) {
                    tempFile.delete()
                    return null
                }
            } else {
                return null
            }
        } ?: return null // return null if cursor is null
    }


    /**
     * Example of usage: val bitmap = decodeSampledBitmapFromUri(contentResolver, contentUri, 1000, 1000)
     * */
    fun decodeSampledBitmapFromUri(contentResolver: ContentResolver, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            // Step 1: Decode with inJustDecodeBounds=true to get image dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Step 2: Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            // Step 3: Decode bitmap with inSampleSize set
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}

fun map(x:Float, in_min:Float, in_max:Float, out_min:Float, out_max:Float):Float {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}



fun List<String>.concatenateWith(divider:String ="",exceptFor:List<Int>):String{
    val stringBuilder = StringBuilder(this.first())

    this.forEachIndexed { index,string->
        if(!exceptFor.contains(index) && index!=0) {
            stringBuilder.append(divider)
            stringBuilder.append(string)
        }
    }
    return stringBuilder.toString()
}



class Insets(view:View){
    val insets = ViewCompat.getRootWindowInsets(view)


    fun getInsetsStatusBar(): Insets? {

        return insets?.getInsets(WindowInsetsCompat.Type.statusBars())

    }
    fun getInsetsNavigationBar(): Insets?{
        return insets?.getInsets(WindowInsetsCompat.Type.navigationBars())
    }
    fun getInsetsIME(): Insets?{
        return insets?.getInsets(WindowInsetsCompat.Type.ime())
    }
    fun getInsetsDisplayCutouts(): Insets?{
        return insets?.getInsets(WindowInsetsCompat.Type.displayCutout())
    }
}



/**
 * Compares the primary list, val1, to val2. If an item is not in val1 but is in val2, it means it was added and val1 needs updating.
 * */
fun <T> differencesBetweenArrays(val1: ArrayList<T>, val2: ArrayList<T>): ArrayList<ModifiedItem<T>>{

    return if(val1.isNotEmpty()){
        val auxarray: ArrayList<ModifiedItem<T>> = ArrayList<ModifiedItem<T>>()

        val2.forEach {
            if(!val1.contains(it)){
                auxarray.add(ModifiedItem(TypeOfUpdate.added,it))
            }
        }

        val1.forEach {
            if(!val2.contains(it)){
                auxarray.add(ModifiedItem(TypeOfUpdate.removed,it))
            }
        }






        auxarray
    }else
        ArrayList<ModifiedItem<T>>()
}





fun setInsetsforItems(mutableArrayList: MutableList<View>,LEFT: Boolean=true,TOP: Boolean=true,RIGHT: Boolean=true,BOTTOM: Boolean=true) {


    if (mutableArrayList.size > 0)
        mutableArrayList.forEach {
        ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.navigationBars() + WindowInsetsCompat.Type.statusBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)


                it.updatePadding(
                    if(LEFT){systemBars.left}else{0},
                    if(TOP){systemBars.top}else{0},
                    if(RIGHT){systemBars.right}else{0},
                    if(BOTTOM){systemBars.bottom}else{0}
                )



                insets
            }
        }
}

fun LogToast(context:Context,message:String){
    Toast.makeText(context,message, Toast.LENGTH_LONG).show()
}





object Files{
    fun Parse(path:String,context:Context):File?{

        val pieces = path.replace(context.filesDir.toString()+"/","").split("/")

        var file1:File = context.filesDir

        pieces.forEach {
            file1= File(file1,it)
        }.also {
            return file1
        }



        return null
    }

}




fun initRecycler(recycler: RecyclerView,context:Context,collumns:Int,adapter: RecyclerView.Adapter<*>):RecyclerView.Adapter<*>{


    recycler.layoutManager = StaggeredGridLayoutManager(collumns, RecyclerView.VERTICAL)

    recycler.adapter= adapter

    adapter.notifyDataSetChanged()

    return adapter

}









// Keep track of ongoing animations

private val runningAnimations = mutableMapOf<View, ValueAnimator>()

fun blipView(
    imageView: View,
    @ColorRes startColor: Int,
    @ColorRes endColor: Int,
    bitmap: Bitmap? = null,
    duration: Long = 500,
    interpolator: Interpolator = AccelerateInterpolator()
) {

    // Cancel any previous animation for this ImageView
    runningAnimations[imageView]?.cancel()
    runningAnimations.remove(imageView)

    // Convert color resources to actual colors
    val startColorInt = ContextCompat.getColor(imageView.context, startColor)
    val endColorInt = ContextCompat.getColor(imageView.context, endColor)

    // Set initial state
    if(imageView is ImageView)
        imageView.setImageBitmap(bitmap)

    // Create a new foreground drawable for tinting
    val foreground = startColorInt.toDrawable()
    imageView.foreground = foreground

    // Create and configure animator
    ValueAnimator.ofArgb(startColorInt, endColorInt).apply {
        this.duration = duration
        this.interpolator = interpolator

        addUpdateListener { animator ->
            val currentColor = animatedValue as Int
            foreground.color = currentColor
            foreground.invalidateSelf()
        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Final cleanup
                imageView.foreground = null
                runningAnimations.remove(imageView)
            }

            override fun onAnimationCancel(animation: Animator) {
                // Clean up if animation was cancelled
                imageView.foreground = null
                runningAnimations.remove(imageView)
            }
        })

        // Store the animation
        runningAnimations[imageView] = this
        start()
    }
}


fun flashView(
    imageView: View,
    @ColorRes flashColor: Int,
    bitmap: Bitmap? = null,
    duration: Long = 500,
    interpolator: Interpolator = AccelerateInterpolator(),
    interpolator2: Interpolator = AccelerateInterpolator()
) {
    // Cancel any previous animation for this view
    runningAnimations[imageView]?.cancel()
    runningAnimations.remove(imageView)

    val flashColorInt = ContextCompat.getColor(imageView.context, flashColor)
    val transparentInt = ContextCompat.getColor(imageView.context, android.R.color.transparent)

    // Set initial state
    if (imageView is ImageView) {
        imageView.setImageBitmap(bitmap)
    }

    val foreground = transparentInt.toDrawable()
    imageView.foreground = foreground


    fun cleanUp() {
        imageView.foreground = null
        runningAnimations.remove(imageView)
    }

    fun startSecondAnimation() {
        // Second animation (flash color -> transparent)
        val secondAnimator = ValueAnimator.ofArgb(flashColorInt, transparentInt).apply {
            this.duration = duration/2
            this.interpolator = interpolator2

            addUpdateListener { animator ->
                foreground.color = animatedValue as Int
                foreground.invalidateSelf()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    cleanUp()
                }

                override fun onAnimationCancel(animation: Animator) {
                    cleanUp()
                }
            })
        }

        runningAnimations[imageView] = secondAnimator
        secondAnimator.start()
    }


    // First animation (transparent -> flash color)
    val firstAnimator = ValueAnimator.ofArgb(transparentInt, flashColorInt).apply {
        this.duration = duration/2
        this.interpolator = interpolator

        addUpdateListener { animator ->
            foreground.color = animatedValue as Int
            foreground.invalidateSelf()
        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Start second animation after first completes
                startSecondAnimation()
            }

            override fun onAnimationCancel(animation: Animator) {
                cleanUp()
            }
        })
    }



    runningAnimations[imageView] = firstAnimator
    firstAnimator.start()
}





fun getAvailableScreenSize(activity: Activity): Point {
    val point = Point()
    val display = activity.windowManager.defaultDisplay
    display.getSize(point)
    return point
}



fun loadImageWithColorTransition(imageView: ImageView,startColor:Int,endColor:Int,Duration:Long, bitmap: Bitmap) {
    // 1. Start with fully tinted view
    imageView.setImageBitmap(bitmap)

    val startColor = ContextCompat.getColor(imageView.context, startColor)
    //ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(startColor))
    imageView.setColorFilter(startColor)

    // 2. Animate from color to transparent
    ValueAnimator.ofArgb(startColor, ContextCompat.getColor(imageView.context, endColor)).apply {
        duration = Duration // Longer duration for better effect
        interpolator = LinearInterpolator()

        addUpdateListener { animator ->

            val currentColor = animatedValue as Int

            imageView.setColorFilter(currentColor)

        }


        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 3. When animation completes, remove tint and show image
                imageView.clearColorFilter()
                //imageView.setImageBitmap(bitmap)
            }
        })

        //start()
    }.start()
}



fun WriteStringInFile(context:Context,filename:String,message:String) {

    try {

        val fos:FileOutputStream = context . openFileOutput ("$filename.txt", Context.MODE_PRIVATE);
        fos.write(message.toByteArray());
        fos.close();
    } catch (e:IOException ) {
        e.printStackTrace();
    }
}




fun AskForPermissionsAtStart(activityContext: Activity, permissions: List<String>,rudimentaryFunction:Boolean=false) {
    if (!rudimentaryFunction)
        for (permission in permissions) {
            try {
                when {
                    ContextCompat.checkSelfPermission(
                        activityContext,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Already granted
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activityContext,
                        permission
                    ) -> {
                        // Fully denied -> explain or redirect
                        if (!VerifyPermission(activityContext, permission)) {
                            OpenAppSettings(activityContext)
                            return
                        }
                    }

                    else -> {
                        // Ask permission
                        ActivityCompat.requestPermissions(activityContext, permissions.toTypedArray(), 0)
                        // After result, you must handle in onRequestPermissionsResult or switch to ActivityResult API
                    }
                }
            } catch (ex: Exception) {
                Log.i("EXCEPTIONS", "Permission exception: ${ex.message}")
            }
        }

    else{
        // just open the damn settings if any permission is missing
        if(!VerifyPermissions(activityContext,permissions)){
            OpenAppSettings(activityContext)
        }
    }
}

fun VerifyPermission(activityContext:Activity,Manifest_permission:String):Boolean{
    return ContextCompat.checkSelfPermission(activityContext,Manifest_permission)== PackageManager.PERMISSION_GRANTED
}
fun VerifyPermissions(activityContext: Activity, Manifest_permissions: List<String>):Boolean {

    var allGood = true

    for(permission:String in Manifest_permissions){
        try {
            if(ContextCompat.checkSelfPermission(activityContext,permission)== PackageManager.PERMISSION_DENIED)
                allGood=false
        }catch (ex: Exception){
            android.util.Log.i("EXCEPTIONS","Permission exception: "+ex.message.toString())
        }
    }


    return allGood
}


fun OpenAppSettings(context: Context?){
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = ("package:" + context?.packageName.toString()).toUri()
    context?.startActivity(intent)
}



fun View.animateTransition(x_or_null:Float?=null, y_or_null: Float? =null,duration:Long=100)
{
    animate()
        .setDuration(duration)
        .apply {
            var a=0;
            if(x_or_null!=null)
            {
                a+=1;
            }
            if(y_or_null!=null)
            {
                a+=10
            }


            when(a){
                0->{}
                1->{
                    this.translationX(x_or_null as Float)
                }
                10->{
                    this.translationX(y_or_null as Float)
                }
                11->{
                    this.translationX(x_or_null as Float)
                    this.translationY(y_or_null as Float)
                }
                else->{}
            }
            this.setDuration(duration)
        }
        .start()

}

fun View.animateLinearMovement(view:View,x_or_null:Float?=null, y_or_null: Float? =null,duration:Long=100,fade_in_out_0:Int=0)
{
    animate()
        .setDuration(duration)
        .apply {
            var a=0;
            if(x_or_null!=null)
            {
                a+=1;
            }
            if(y_or_null!=null)
            {
                a+=10
            }


            when(a){
                0->{}
                1->{
                    this.translationX(x_or_null as Float)
                }
                10->{
                    this.translationY(y_or_null as Float)
                }
                11->{
                    this.translationX(x_or_null as Float)
                    this.translationY(y_or_null as Float)
                }
                else->{}
            }

            if(fade_in_out_0>0)
            {
                this.alpha(1F)
                this.withEndAction{
                    view.visibility=View.VISIBLE
                }


            }
            else if(fade_in_out_0<0)
            {
                this.alpha(0F)
                this.withEndAction {
                    view.visibility = View.INVISIBLE // or View.GONE
                }
            }

        }

        .start()

}






@Composable
fun HideKeyboard(){
    //val keyboardController= LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusmanager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Hide the keyboard when the "Done" action is performed
                    keyboardController?.hide()
                    focusmanager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Hide the keyboard when the button is clicked
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hide Keyboard")
        }
    }
}





@RequiresApi(Build.VERSION_CODES.R)
fun setSystemBarToSwipeUp(window:Window){
    //val imecontroller = this.window.insetsController
    //imecontroller?.systemBarsBehavior

    val barscontroller = WindowInsetsControllerCompat(window,window.decorView)

    barscontroller.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    barscontroller.hide(WindowInsetsCompat.Type.navigationBars())

    //val notificationbarcontroller=window.insetsController
    //notificationbarcontroller?.hide(WindowInsetsCompat.Type.statusBars())


}

fun adjustViewsForKeyboard(view:View){
    ViewCompat.setOnApplyWindowInsetsListener(view){v,insets->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

        val systembarInsets=insets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.setPadding(0,0,0,systembarInsets.bottom)

        insets
    }
}





fun uninstall(context: Context, packageName:String){
    Log.i("MYTAG","Main activity destroy");

    val packageName = packageName // Get the current app's package name
    val uninstall = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
    context.startActivity(uninstall);
}

fun CustomSnack(whereToShowIt: View, message: String,onClickAction: (() -> Unit)? = null){
    val snack = Snackbar.make(whereToShowIt,message, Snackbar.LENGTH_SHORT)
    snack.animationMode=Snackbar.ANIMATION_MODE_FADE
    snack.apply {
        view.setOnClickListener {
            dismiss()
            onClickAction
        }

        behavior=object:BaseTransientBottomBar.Behavior() {
            override fun canSwipeDismissView(child: View): Boolean {
                return true
            }
        }

        (view.layoutParams as FrameLayout.LayoutParams).apply {
            width= ActionBar.LayoutParams.WRAP_CONTENT;
            gravity= Gravity.END or Gravity.BOTTOM;
        }

        view.apply {
            setBackgroundColor( ContextCompat.getColor(context,R.color.gray))

            setAnchorView(whereToShowIt)

        }
    }.show()
}

fun CustomSnack2(whereToShowIt: View, message: String,clickButtonText:String?=null,onClickAction: (() -> Unit)? = null){
    val snack = Snackbar.make(whereToShowIt,message, Snackbar.LENGTH_LONG)
        .setAction(clickButtonText, View.OnClickListener(){
            onClickAction?.invoke()
        })

    snack.animationMode=Snackbar.ANIMATION_MODE_FADE
    snack.apply {

        view.setOnClickListener {
            dismiss()
        }


        (view.layoutParams as FrameLayout.LayoutParams).apply {
            //width= ActionBar.LayoutParams.WRAP_CONTENT;
        }

    }
    snack.show()
}


fun customToast(whereToShowIt: View, context: Context, message: String) {
    val tost = Toast.makeText(context, message, Toast.LENGTH_SHORT);


    tost.show();
}
/** Use getInt, getString, etc to retrieve from it.
 *
 */
fun getSharedPrefferencesStorage(context:Context): SharedPreferences? {
    return context.getSharedPreferences("My SF",Context.MODE_PRIVATE);
}


/** Use putInt, putString, etc to put into the editor.
 * Needs to be closed with apply()
 *
 * Returns the shared preferences editor for putting values inside
 */
fun getSharedPrefferencesEditor(context:Context?=null): SharedPreferences.Editor? {
    var editor : SharedPreferences.Editor;
    if(context==null) {
        var sf: SharedPreferences =
            Application.instance.getSharedPreferences("My SF", Context.MODE_PRIVATE);

        return sf.edit()
    }
    else{
        var sf: SharedPreferences =
            context.getSharedPreferences("My SF", Context.MODE_PRIVATE);

        return sf.edit()
    }
}







fun sendPageToLeft(context: Context):Bundle{

    val aux = ActivityOptions.makeCustomAnimation(
        context,
        R.anim.slide_from_right,
        R.anim.slide_to_left
    )
    return aux.toBundle();
}

fun sendPageToRight(context: Context):Bundle{
    val aux= ActivityOptions.makeCustomAnimation(
        context,
        R.anim.slide_from_left,
        R.anim.slide_to_right
    )
    return aux.toBundle();
}


fun saveAsJson(context: Context, filename:String, data:Any,parent:File=context.filesDir) {

    if(!parent.exists()){
        parent.mkdirs()
    }

    val json=Gson().toJson(data);
    val Filename= "$filename.json"

    File(parent,Filename).writeText(json)
}


inline fun <reified T> loadFromJson(context:Context,filename: String,data: T,parent:File=context.filesDir): T {
    val Filename= "$filename.json"
    val file=File(parent,Filename)

    if(file.exists()) {
        val loadedFile = file.readText()
        val type = object:TypeToken<T>(){}.type
        val readData:T = Gson().fromJson(loadedFile, type)
        return readData
    }
    return data
}





 fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {

    val stream = ByteArrayOutputStream()

    bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream)

    val byteArray = stream.toByteArray()

    return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size, BitmapFactory.Options().apply { inScaled=true;inTargetDensity=10;inDensity=20 })
}

class ViewAttributes(public val view:View):Object(){


    inner class ForegroundTint{
        public fun Get(): ColorStateList? {
            return view.foregroundTintList
        }

        public fun Compare(context:Context,colorId:Int):Boolean{
            return view.foregroundTintList== ColorStateList.valueOf(getColor(context,colorId))
        }

        public fun Set(context:Context,colorId:Int){
            view.foregroundTintList= ColorStateList.valueOf(getColor(context,colorId))
        }
    }

    inner class BackgroundTint{
        public fun Get(): ColorStateList? {
            return view.backgroundTintList
        }

        public fun Compare(context:Context,colorId:Int):Boolean{
            return view.backgroundTintList== ColorStateList.valueOf(getColor(context,colorId))
        }

        public fun Set(context:Context,colorId:Int){
            view.backgroundTintList= ColorStateList.valueOf(getColor(context,colorId))
        }
    }

    inner class Background{
        public fun Get(): Drawable? {
            return view.background
        }

        public fun Compare(context:Context,drawableId:Int):Boolean{
            return view.background== ContextCompat.getDrawable(context,drawableId)
        }

        public fun Set(context:Context,drawableId:Int){
            view.background=ContextCompat.getDrawable(context,drawableId)
        }
    }

    inner class BackgroundTintMode{
        public fun Get(): PorterDuff.Mode? {
            return view.backgroundTintMode
        }

        public fun Compare(PorterduffMode: PorterDuff.Mode):Boolean{
            return view.backgroundTintMode==PorterduffMode
        }

        public fun Set(PorterduffMode: PorterDuff.Mode){
            view.backgroundTintMode= PorterduffMode
        }
    }

    inner class Edges{
        var Bottom:Int
            get() {return view.bottom}
            set(value:Int){view.bottom=value}

        var Top:Int
            get() {return view.top}
            set(value) {view.top=value}

        var Left: Int
            get() {return view.left}
            set(value:Int){view.left=value}

        var Right: Int
            get() {return view.right}
            set(value:Int){view.right=value}
        var TopLeft: List<Int>
            get() {return listOf(view.top, view.left)}
            set(value: List<Int>) {
                if (value.size == 2)
                    view.top = value[0]
                view.left = value[1]
            }
        var TopRight: List<Int>
            get() {return listOf(view.top, view.right)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.top = value[0]
                    view.right = value[1]
                }
            }
        var BottomLeft: List<Int>
            get() {return listOf(view.bottom,view.left)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.bottom = value[0]
                    view.left = value[1]
                }
            }
        var BottomRight: List<Int>
            get() {return listOf(view.bottom,view.right)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.bottom = value[0]
                    view.right = value[1]
                }
            }


    }
}