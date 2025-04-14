package StorageTest.Classes

import GlobalValues.Media
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil
import java.net.URI

class FileManager(val context: Activity) {



    private val PICK_IMAGE_REQUEST= 573842
    private val PICK_IMAGES_REQUEST= 18527




    fun pickSinglePicture(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        context.startActivityForResult(intent,PICK_IMAGE_REQUEST)
    }
    fun pickMultiplePictures(){
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)

        context.startActivityForResult(intent,PICK_IMAGES_REQUEST)
    }

    fun onActivityResultSinglePicture(requestCode: Int, resultCode: Int, data: Intent?): Uri? {
        if(requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            return data.data
        }
        else
            return null
    }
    fun onActivityResultMultiplePictures(requestCode: Int, resultCode: Int, data: Intent?):List<Uri?>?{
        if(requestCode==PICK_IMAGES_REQUEST && resultCode== Activity.RESULT_OK && data!=null){
            return data.clipData as List<Uri?>
        }
        else
            return null
    }

}