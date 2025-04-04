package StorageTest

import Functions.CustomSnack2
import Functions.Extensions.Companion.isCorrupted
import Functions.getAvailableScreenSize
import GlobalValues.Media.internalPictureList
import StorageTest.Classes.InternalStoragePhoto
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import androidx.activity.result.launch
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Function
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.delay
import java.io.File
import java.util.Queue
import java.util.Stack

class StorageMainActivity : AppCompatActivity(), Adapter_InternalStoragePhoto.onClickListener, Adapter_InternalStoragePhoto.onLongPressListener {

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

        lifecycleScope.launch {
            reinitializeInternalRecycler()

            recycler.adapter?.notifyDataSetChanged()
        }.start()









        // contract between activity and camera to get a photo and tell the camera to save the photo
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            val isPrivate = switchPrivate.isChecked

            if(isPrivate){
                val saveSuccess = savePhotoToInternalStorage(UUID.randomUUID().toString(),it)
                if(saveSuccess){


                    //Toast.makeText(this,"Photo saved successfully",Toast.LENGTH_SHORT).show()

                    recycler.adapter?.notifyDataSetChanged()
                }
                else{
                    //Toast.makeText(this,"Failed to save photo",Toast.LENGTH_SHORT).show()
                }
            }
            else{

            }
        }


        button_Take_Photo.setOnClickListener {
            takePhoto.launch()
        }


        findViewById<TextView>(R.id.textView2).setOnClickListener {
            Toast.makeText(this,GlobalValues.Media.internalPictureList.size.toString(),Toast.LENGTH_SHORT).show()
        }



    }



    private suspend fun reinitializeInternalRecycler() {
        GlobalValues.Media.internalPictureList= loadPhotosFromInternalStorage() as ArrayList<InternalStoragePhoto>
        Functions.saveAsJson(context, "Lista_Imagini", internalPictureList)
        recycler=findViewById<RecyclerView>(R.id.internalRecycler)
        list_of_deleted_pics= ArrayList<DeletedItem>()
        nestedscrollview=findViewById(R.id.nestedScrollView2)

        val screenSizes = getAvailableScreenSize(this)

        recycleAdapter = Adapter_InternalStoragePhoto(
            internalPictureList,
            context,
            screenSizes,
            this,
            this
        )
        recycler.adapter = recycleAdapter

        recycler.layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }





    private fun deletePhotoFromInternalStorage(filename: String, position: Int): Boolean {
        return try {
            val adapter = recycler.adapter as Adapter_InternalStoragePhoto
            var currentList = adapter.mList

            val adaptor = (recycler.adapter as Adapter_InternalStoragePhoto)


            // Get the item from current adapter position
            val deletedPic = currentList[position]

            // Store for undo
            list_of_deleted_pics.add(DeletedItem(deletedPic, position))

            // Remove from adapter's list



            (recycler.adapter as Adapter_InternalStoragePhoto).removeAt(position)
            currentList = adapter.mList

            // Save changes
            adaptor.saveToJson("Lista_Imagini")


            CustomSnack2(nestedscrollview,"Picture deleted","Undo") {

                list_of_deleted_pics.reversed().forEach {
                    (recycler.adapter as Adapter_InternalStoragePhoto).insertAt(it.position,it.item)

                }
                list_of_deleted_pics.clear()

                adaptor.saveToJson("Lista_Imagini")


            }

            //Toast.makeText(context,"Deleted a photo",Toast.LENGTH_SHORT).show()
            true
        }catch (ex: Exception){
            ex.printStackTrace()
            //Toast.makeText(this,"Failed to delete a photo",Toast.LENGTH_SHORT).show()
            false
        }
    }


    private suspend fun loadPhotosFromInternalStorage():List<InternalStoragePhoto>{

                                                    // coroutine starter, google it
        var photo_list:List<InternalStoragePhoto> = withContext(Dispatchers.IO){

            val files = filesDir.listFiles()

//            files
//                ?.filter{it.canRead() && it.isFile &&  it.name.endsWith(".jpg")}
//                ?.forEach {  }


            files?.filter { it.canRead() && it.isFile &&  it.name.endsWith(".jpg") }?.map{

                val bytes = it.readBytes()

                val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)


                InternalStoragePhoto(it.name,bitmap)
            }?:listOf()
        }

        return photo_list
    }


    // Returns false if photo was not saved.
    private fun savePhotoToInternalStorage(filename: String, bitmap: Bitmap?): Boolean {
        // Early return if bitmap is null
        if (bitmap == null) {
            Log.e("PhotoSave", "Cannot save null bitmap")
            return false
        }

        val fullFilename = "$filename.jpg"
        val file = File(fullFilename) // Use proper context path

        return try {
            // Use auto-closing stream
            openFileOutput(fullFilename,MODE_PRIVATE).use { outputStream ->
                // Compress and write the bitmap
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)) {
                    throw IOException("Failed to compress bitmap")
                }
            }

            // Verify the file was created properly
            if(!file.isCorrupted)
            {
                (recycler.adapter as Adapter_InternalStoragePhoto).insertAt((recycler.adapter as Adapter_InternalStoragePhoto).mList.size,InternalStoragePhoto(fullFilename, bitmap))
            }
            // Only add to list after successful save

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

        deletePhotoFromInternalStorage(internalPictureList[position].name,position)

    }

    override fun onDestroy() {
        super.onDestroy()
        list_of_deleted_pics.forEach {
            //item->deleteFile(item.item.name)
            //(recycler.adapter as Adapter_InternalStoragePhoto<InternalStoragePhoto>).mList.removeAt(item.position)
        }


        Functions.saveAsJson(context,"Lista_Imagini",(recycler.adapter as Adapter_InternalStoragePhoto).mList)
    }


    override fun onResume() {
        super.onResume()


        lifecycleScope.launch {
            reinitializeInternalRecycler()
            recycler.adapter?.notifyDataSetChanged()
        }




    }




}