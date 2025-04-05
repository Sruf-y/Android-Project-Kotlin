package StorageTest

import Functions.getAvailableScreenSize
import StorageTest.Classes.InternalStoragePhoto
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.composepls.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.activity.result.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import java.io.File


class StorageMainActivity : AppCompatActivity(), Adapter_InternalStoragePhoto.onClickListener, Adapter_InternalStoragePhoto.onLongPressListener {

    private lateinit var photoDirectory:File

    class DeletedItem(val item: InternalStoragePhoto,val position: Int){
    }

    lateinit var nestedscrollview: NestedScrollView
    lateinit var recycleAdapter: Adapter_InternalStoragePhoto
    lateinit var context:Context
    lateinit var recycler: RecyclerView
    lateinit var list_of_deleted_pics: ArrayList<DeletedItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        context=this

        val switchPrivate:SwitchCompat = findViewById(R.id.switchPrivate)
        val button_Take_Photo: ImageView = findViewById(R.id.btnTakePhoto)
        photoDirectory= File(this.filesDir,"GalleryPics")

        lifecycleScope.launch {
            reinitializeInternalRecycler()

            recycler.adapter?.notifyDataSetChanged()
        }.start()









        // contract between activity and camera to get a photo and tell the camera to save the photo
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = switchPrivate.isChecked

            if (isPrivate) {
                val saveSuccess = recycleAdapter.savePictureToFile(
                    UUID.randomUUID().toString(),
                    it,
                    photoDirectory
                )
            }
        }

        button_Take_Photo.setOnClickListener {
            takePhoto.launch()
        }

    }



    private suspend fun reinitializeInternalRecycler() {


        recycler=findViewById<RecyclerView>(R.id.internalRecycler)
        list_of_deleted_pics= ArrayList<DeletedItem>()
        nestedscrollview=findViewById(R.id.nestedScrollView2)

        val screenSizes = getAvailableScreenSize(this)

        recycleAdapter = Adapter_InternalStoragePhoto(
            ArrayList<InternalStoragePhoto>(),
            context,
            screenSizes,
            photoDirectory,
            this,
            this
        )

        recycleAdapter.updateData(recycleAdapter.loadPicturesFromFiles(photoDirectory) as ArrayList<InternalStoragePhoto>)

        recycler.adapter = recycleAdapter

        recycler.layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }














    override fun onPictureClick(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {

        //TODO() // new activity where i display the picture in a mapview to be able to change size and scroll in all diractions

    }

    override fun onPictureLongClick(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {

        recycleAdapter.deletePhotoFromStorageAtPosition(nestedscrollview,position)

    }

    override fun onPause() {
        // Always call super first
        super.onPause()

        // Use viewModelScope or lifecycleScope instead of GlobalScope
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val adapter = recycler.adapter as? Adapter_InternalStoragePhoto ?: return@launch
                val currentList = ArrayList(adapter.mList) // Create a safe copy

                // 1. Delete marked files
                adapter.deleteMarkedItems()

                // 2. Save remaining photos
                adapter.savePicturesToFiles(photoDirectory)

                // 3. Persist the updated list
                adapter.saveDataToJson("Lista_Imagini",photoDirectory)
            } catch (e: Exception) {
                Log.e("Cleanup", "Critical error during cleanup", e)
            }
        }
    }


    override fun onResume() {
        super.onResume()

        photoDirectory = File(this.filesDir,"GalleryPics")

        lifecycleScope.launch {
            reinitializeInternalRecycler()
            recycler.adapter?.notifyDataSetChanged()
        }




    }




}