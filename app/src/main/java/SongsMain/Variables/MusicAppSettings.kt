package SongsMain.Variables

import SongsMain.Tutorial.Application
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.composepls.R
import java.io.File

object MusicAppSettings {

//    fun getDarkModeRadioButton(): Drawable? {
//        val darkContext = Application.instance.createConfigurationContext(
//            Configuration().apply {
//                uiMode = Configuration.UI_MODE_NIGHT_YES or (Application.instance.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK)
//            }
//        )
//
//        val drawable = AppCompatResources.getDrawable(darkContext, android.R.drawable.btn_radio)
//        return drawable
//    }


    var theme:Int = R.drawable.gradient_right_corner

    var orientation:Int = 0



    fun applySettings(mains: MutableList<ConstraintLayout>?,alsoDoRestore:(()->Unit)?=null){

        if(mains!=null){
            mains.forEach { it->
                it.background= ContextCompat.getDrawable(Application.instance,MusicAppSettings.theme)
            }
        }


        if(alsoDoRestore!=null){
            alsoDoRestore.invoke()
        }

    }


    fun restoreSettings(){
        MusicAppSettings.theme=Functions.loadFromJson(Application.instance,"Background Theme",R.drawable.gradient_right_corner,File(
            Application.instance.filesDir,"Settings"))

        MusicAppSettings.orientation=Functions.loadFromJson(Application.instance,"Orientation",0,File(
            Application.instance.filesDir,"Settings"))




    }
}