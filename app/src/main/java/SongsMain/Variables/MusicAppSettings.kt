package SongsMain.Variables

import SongsMain.Tutorial.Application
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.composepls.R
import java.io.File

object MusicAppSettings {


    var theme:Int = R.drawable.gradient_right_corner

    var orientation:Int = 0



    fun applySettings(mains: MutableList<ConstraintLayout>?){

        if(mains!=null){
            mains.forEach { it->
                it.background= ContextCompat.getDrawable(Application.instance,MusicAppSettings.theme)
            }
        }

    }


    fun restoreSettings(){
        MusicAppSettings.theme=Functions.loadFromJson(Application.instance,"Background Theme",R.drawable.gradient_right_corner,File(
            Application.instance.filesDir,"Settings"))

        MusicAppSettings.orientation=Functions.loadFromJson(Application.instance,"Orientation",0,File(
            Application.instance.filesDir,"Settings"))

    }
}