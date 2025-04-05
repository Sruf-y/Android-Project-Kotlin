package StorageTest

import Functions.CustomSnack2
import Functions.Extensions.Companion.isCorrupted
import Functions.blipView
import Functions.flashView
import StorageTest.Classes.InternalStoragePhoto
import android.view.View
import android.view.ViewGroup
import com.example.composepls.R
import Functions.loadImageWithColorTransition
import Functions.saveAsJson
import StorageTest.StorageMainActivity.DeletedItem
import Utilities.Utils.Companion.dP
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.AccelerateInterpolator
import androidx.core.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Adapter_InternalStoragePhoto(val mList:ArrayList<InternalStoragePhoto>, var context:Context, val screensize:Point,var defaultDirectory:File=context.filesDir,val listener: onClickListener, val listener2: onLongPressListener):
    Adapter<ViewHolder>(){
    val items_marked_for_deletion: ArrayList<DeletedItem> = ArrayList<DeletedItem>()


    interface onClickListener {
        fun onPictureClick(position: Int,itemViewHolder: ViewHolder)
    }

    interface onLongPressListener{
        fun onPictureLongClick(position: Int,itemViewHolder: ViewHolder)
    }



    class Photo(itemView: View): ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageView)
        val constraintLayout = itemView.findViewById<ConstraintLayout>(R.id.constr)


    }


    fun updateData(lista: ArrayList<InternalStoragePhoto>) {
        mList.clear()
        mList.addAll(lista)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.photo_example,parent,false)

        return Photo(itemview)
    }



    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val photo = mList[position]



        (holder as Photo)

        val aspectRadio = photo.bitmap.width.toFloat() / photo.bitmap.height.toFloat()

        ConstraintSet().apply {
            clone(holder.constraintLayout)
            setDimensionRatio(holder.imageView.id, aspectRadio.toString())
            applyTo(holder.constraintLayout)
        }


        val radius:Float = 10F.dP.toFloat();
        holder.imageView.setShapeAppearanceModel(holder.imageView.getShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED,radius)
            .build());



        holder.imageView.setImageBitmap(photo.bitmap)


        //usage
        loadImageWithColorTransition(
            holder.imageView,
            R.color.semi_transparent,
            R.color.transparent,
            200L,
            photo.bitmap
        )






        holder.itemView.setOnClickListener {
            //flashCard(position,holder)
            true
        }
        holder.itemView.setOnLongClickListener {
            i->listener2.onPictureLongClick(position,holder)
            true
        }
    }



    override fun getItemCount(): Int {
        return mList.size
    }

    fun removeAt(position: Int){
    mList.removeAt(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(position, mList.size);
    }

    fun insertAt(position:Int,data: InternalStoragePhoto){
        mList.add(position, data)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, mList.size);
    }

    fun saveDataToJson(filename:String, parent:File=defaultDirectory){
        saveAsJson(context, filename, mList,parent)
    }

    fun loadDataFromJson(filename:String,parent:File=defaultDirectory){

        val lista: ArrayList<InternalStoragePhoto> = Functions.loadFromJson(context, filename, mList, parent)
        mList.clear()
        mList.addAll(lista)
        notifyItemRangeChanged(0, itemCount)
    }


    fun savePicturesToFiles(toDirectory: File=defaultDirectory): Boolean{
        return try{
            mList.forEach {
                savePictureToFile(it.name,it.bitmap,toDirectory)
            }
            true
        }catch(e: Exception){
            e.printStackTrace()
            false
        }
    }


    fun savePictureToFile(filename: String, bitmap: Bitmap?,toDirectory:File=defaultDirectory): Boolean {

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

        return try {
            // Use auto-closing stream
            FileOutputStream(file).use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)) {
                    throw IOException("Failed to compress bitmap")
                }
            }

            // Verify the file was created properly
            if (!file.isCorrupted) {
                insertAt(mList.size, InternalStoragePhoto(fullFilename, bitmap))
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
    suspend fun loadPicturesFromFiles(directoryPath:File):List<InternalStoragePhoto>{


        // coroutine starter, google it
        var photo_list: List<InternalStoragePhoto> = withContext(Dispatchers.IO) {

            val files = directoryPath.listFiles()


            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                ?.map {

                    val bytes = it.readBytes()

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val safeName = it.name.replace(".jpg", "") + ".jpg"


                    InternalStoragePhoto(it.name, bitmap)
                }
                ?.distinctBy { p -> p.name }
                ?: listOf()
        }

        return photo_list

    }

     fun deletePhotoFromStorageAtPosition(viewToAttachSnackTo:View,position: Int,undoStorage:ArrayList<DeletedItem> = items_marked_for_deletion): Boolean {
        return try {

            // Get the item from current adapter position
            val deletedPic = mList[position]

            // Store for undo
            undoStorage.add(DeletedItem(deletedPic, position))

            // Remove from adapter's list

            removeAt(position)




            CustomSnack2(viewToAttachSnackTo,"Picture deleted","Undo") {

                undoStorage.reversed().forEach {insertAt(it.position,it.item)

                }
                undoStorage.clear()

            }

            true
        }catch (ex: Exception){
            ex.printStackTrace()
            false
        }
    }

    fun deleteMarkedItems(undoStorage:ArrayList<DeletedItem> = items_marked_for_deletion,parent:File=defaultDirectory){
        undoStorage.forEach {
            File(parent,it.item.name).apply {
                if(exists())
                    delete()
            }
        }
    }



    fun clear(){
        val size = mList.size
        mList.clear()
        notifyItemRangeChanged(0,size)
    }
    fun dump(): ArrayList<InternalStoragePhoto>{
        val size = mList.size
        val tempList = mList
        mList.clear()
        notifyItemRangeChanged(0,size)

        return tempList
    }


    fun blipCard(position: Int, holder: ViewHolder) {
        blipView(
            (holder as Photo).imageView,
            R.color.activated,
            R.color.transparent,
            mList[position].bitmap,
            500L,
            DecelerateInterpolator()
        )
    }

    fun flashCard(position: Int,holder: ViewHolder){
        flashView(
            imageView = (holder as Photo).imageView,
            flashColor = R.color.flash_glow_for_items,
            bitmap = mList[position].bitmap,
            duration = 300,
            AccelerateInterpolator(),
            AccelerateInterpolator()
        )
    }



}