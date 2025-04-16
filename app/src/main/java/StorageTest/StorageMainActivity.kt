package StorageTest

import DataClasses_Ojects.Logs
import Functions.getAvailableScreenSize
import StorageTest.Classes.FileManager
import StorageTest.Classes.InternalStoragePhoto
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.composepls.R
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.github.panpf.zoomimage.GlideZoomImageView
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import java.io.File
import androidx.core.graphics.scale
import androidx.recyclerview.widget.GridLayoutManager


class StorageMainActivity : AppCompatActivity(), Adapter_InternalStoragePhoto.onClickListener,
    Adapter_InternalStoragePhoto.onLongPressListener {

    val fileManager = FileManager(this)

    private lateinit var photoDirectory: File
    private lateinit var UUidstring: String

    private fun generateNewUUID(): String {
        UUidstring = UUID.randomUUID().toString()
        return UUidstring
    }

    class DeletedItem(val item: InternalStoragePhoto, val position: Int) {
    }

    lateinit var nestedscrollview: NestedScrollView
    lateinit var recycleAdapter: Adapter_InternalStoragePhoto
    lateinit var context: Context
    lateinit var recycler: RecyclerView


    lateinit var  switchPrivate: SwitchCompat

    lateinit var currentPhotoFile: File

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Image captured and saved to fileUri specified in the intent


            lifecycleScope.launch {

                val file_to_fix = Functions.Images.loadFromFile(currentPhotoFile)
                Functions.Images.saveToFile(
                    currentPhotoFile.name,
                    file_to_fix,
                    File(currentPhotoFile.parent)
                )
                recycleAdapter.insertAt(
                    0,
                    InternalStoragePhoto(currentPhotoFile.name, file_to_fix,currentPhotoFile)
                )
            }.invokeOnCompletion {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }



            Log.d("CameraManager", "Picture was taken successfully")
            //If the result is ok, then MYFILE contains the picture.
        } else {
            Log.d("CameraManager", "Picture was not taken")
            currentPhotoFile.delete() // Delete the file if the picture wasn't taken
        }
    }

    fun takePicture(context: Context, MYFILE: File) {

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request camera permission if not granted
            Log.e("CameraManager", "Camera permission not granted")
            return
        }
        currentPhotoFile = MYFILE
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoURI: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            currentPhotoFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        takePictureLauncher.launch(takePictureIntent)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        context = this

        switchPrivate = findViewById(R.id.switchPrivate)
        val button_Take_Photo: ImageView = findViewById(R.id.btnTakePhoto)
        photoDirectory = File(this.filesDir, "GalleryPics")
        if(!photoDirectory.exists())
            photoDirectory.mkdir()

        if (!photoDirectory.exists())
            photoDirectory.mkdir()




        reinitializeInternalRecycler()




















        button_Take_Photo.setOnClickListener {
            if (switchPrivate.isChecked)
                takePicture(context, File(photoDirectory, "JPEG_" + generateNewUUID() + ".jpg"))
            else
                fileManager.pickSinglePicture()

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = fileManager.onActivityResultSinglePicture(requestCode, resultCode, data)

        if (result != null) {
            lifecycleScope.launch {
                val file = Functions.Images.loadFromUri(context, result)

                if (file != null) {
                    recycleAdapter.insertAt(
                        0,
                        InternalStoragePhoto(
                            file.name,
                            Functions.Images.loadFromFile(file),
                            file
                        )
                    )
                }
            }.invokeOnCompletion {
                recycleAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun reinitializeInternalRecycler() {


        recycler = findViewById<RecyclerView>(R.id.internalRecycler)
        nestedscrollview = findViewById(R.id.nestedScrollView2)

        val screenSizes = getAvailableScreenSize(this)

        recycleAdapter = Adapter_InternalStoragePhoto(
            ArrayList<InternalStoragePhoto>(),
            context,
            screenSizes,
            photoDirectory,
            this,
            this
        )


        recycler.adapter = recycleAdapter

        lifecycleScope.launch {
            recycleAdapter.loadPicturesFromFiles(photoDirectory).apply {
                if (isNotEmpty()) {
                    val spanCount = if(getResources().configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                        3
                    else{
                        5
                    }
                    recycler.layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
                    recycleAdapter.updateData(this as ArrayList<InternalStoragePhoto>)
                }
            }
        }.invokeOnCompletion {
            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
        }



    }


    override fun onPictureClick(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {









        //TODO() // new activity where i display the picture in a mapview to be able to change size and scroll in all diractions

        val bringUpFront = findViewById<GlideZoomImageView>(R.id.imageView2)
        //bringUpFront.setImageBitmap(recycleAdapter.mList[position].bitmap)


//        lifecycleScope.launch {
//            var photo_list: List<InternalStoragePhoto> = withContext(Dispatchers.IO) {
//
//                val files = photoDirectory.listFiles()
//
//
//                files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") && it.exists() && it.name == recycleAdapter.mList[position].name }
//                    ?.map {
//
//                        val bytes = it.readBytes()
//
//                        // artificial "quality loss-less" compression that doesn't actually compress data
//                        val bitmap = BitmapFactory.decodeByteArray(
//                            bytes,
//                            0,
//                            bytes.size,
//                            BitmapFactory.Options()
//                                .apply { inScaled = true;inTargetDensity = 5;inDensity = 10 })
//
//
//
//                        InternalStoragePhoto(it.name, bitmap)
//                    }
//                    ?.distinctBy { p -> p.name }
//                    ?: listOf()
//            }


        // Get the file path of the original image
        val imageFile = File(photoDirectory, recycleAdapter.mList[position].name)

        // Set up subsampling FIRST
        if (imageFile.exists()) {
            val result = ImageSource.fromFile(imageFile)
            bringUpFront.setSubsamplingImage(result)
        } else {
            Log.e("Image display", "File does not exist: ${imageFile.path}")
        }

        // Set a placeholder image (optional, but recommended)
        // This will show if subsampling fails or is not supported
        // ... you can replace this with your own placeholder bitmap or resource ID ...
        val originalPlaceholderBitmap = recycleAdapter.mList[position].bitmap


        if(originalPlaceholderBitmap!=null) {
            Log.i(Logs.MEDIA_IMAGES.toString(),"Clicked picture-- Width: "+originalPlaceholderBitmap.width.toString()+" | Height: "+originalPlaceholderBitmap.height.toString())


            val width = originalPlaceholderBitmap.width // Adjust the division factor as needed
            val height = originalPlaceholderBitmap.height // Adjust the division factor as needed

            val factor = if (width+height>10000) {
                2
            } else {
                1
            }

            val resizedPlaceholderBitmap = originalPlaceholderBitmap.scale(width/factor, height/factor, false)


            bringUpFront.setImageBitmap(resizedPlaceholderBitmap)


        }






        bringUpFront.visibility = View.VISIBLE
        bringUpFront.setOnClickListener {
            bringUpFront.visibility = View.GONE
            //}
        }
    }

    override fun onPictureLongClick(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {

        recycleAdapter.deletePhotoFromStorageAtPosition(nestedscrollview, position)

    }

    override fun onPause() {
        // Always call super first
        super.onPause()

        // Use viewModelScope or lifecycleScope instead of GlobalScope

            try {


                // 2. Save remaining photos
                recycleAdapter.savePicturesToFiles(photoDirectory)
                recycleAdapter.deleteMarkedItems()

            } catch (e: Exception) {
                Log.e("Cleanup", "Critical error during cleanup", e)
            }

    }



    override fun onResume() {
        super.onResume()


    }

}