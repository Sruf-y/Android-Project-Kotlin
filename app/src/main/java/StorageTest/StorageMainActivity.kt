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
import androidx.core.widget.NestedScrollView
import java.io.File
import java.util.Stack

class StorageMainActivity : AppCompatActivity(), Adapter_InternalStoragePhoto.onClickListener,
    Adapter_InternalStoragePhoto.onLongPressListener {


    lateinit var nestedscrollview: NestedScrollView
    lateinit var recycleAdapter: Adapter_InternalStoragePhoto<InternalStoragePhoto>
    lateinit var context:Context
    lateinit var recycler: RecyclerView
    lateinit var list_of_deleted_pics: Stack<InternalStoragePhoto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_main)

        context=this
        nestedscrollview=findViewById(R.id.nestedScrollView2)
        val switchPrivate:SwitchCompat = findViewById(R.id.switchPrivate)
        val button_Take_Photo: ImageView = findViewById(R.id.btnTakePhoto)
        recycler=findViewById<RecyclerView>(R.id.internalRecycler)
        list_of_deleted_pics= Stack<InternalStoragePhoto>()


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




        lifecycleScope.launch {
            reinitializeInternalRecycler()
        }
        refreshListAndRecyclerFromStorage()

        recycler.adapter?.notifyDataSetChanged()





        // contract between activity and camera to get a photo and tell the camera to save the photo
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
            val isPrivate = switchPrivate.isChecked

            if(isPrivate){
                val saveSuccess = savePhotoToInternalStorage(UUID.randomUUID().toString(),it)
                if(saveSuccess){

                    refreshListAndRecyclerFromStorage()
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


    private fun refreshListAndRecyclerFromStorage() {
        lifecycleScope.launch {

            recycleAdapter.updateData(internalPictureList)


            // Optional: Save to JSON
            withContext(Dispatchers.IO) {
                Functions.saveAsJson(context, "Lista_Imagini", internalPictureList)
            }
        }
    }


    private fun deletePhotoFromInternalStorage(filename: String,position: Int): Boolean{
        return try{



            val deletedpic = internalPictureList.filter { p->p.name==filename }

            list_of_deleted_pics.push(deletedpic.first())

            internalPictureList.removeAt(position)

            recycler.adapter?.notifyItemRemoved(position)


            Functions.saveAsJson(context, "Lista_Imagini", internalPictureList)

            CustomSnack2(nestedscrollview,"Picture deleted","Undo") {

                internalPictureList.add(position, list_of_deleted_pics.pop())
                recycler.adapter?.notifyItemInserted(position)

                Functions.saveAsJson(context, "Lista_Imagini", internalPictureList)

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
                GlobalValues.Media.internalPictureList.add(InternalStoragePhoto(fullFilename, bitmap))
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
            file->deleteFile(file.name)
            internalPictureList.remove(file)
        }


        Functions.saveAsJson(context,"Lista_Imagini",GlobalValues.Media.internalPictureList)
    }
//    override fun onPause() {
//        super.onPause()
//
//        list_of_deleted_pics.forEach { file->deleteFile(file.name) }
//
//        Functions.saveAsJson(context,"Lista_Imagini",GlobalValues.Media.internalPictureList)
//    }

    override fun onResume() {
        super.onResume()


        lifecycleScope.launch {
            reinitializeInternalRecycler()
        }
        refreshListAndRecyclerFromStorage()
        recycler.adapter?.notifyDataSetChanged()


    }




}