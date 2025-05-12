package SongsMain.Settings

import Functions.loadFromJson
import SongsMain.Tutorial.Application
import android.view.View
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.composepls.R
import java.io.File

object MusicAppSettings {


    val theme
        get()=themesList[
                if(themeIndex<themesList.size){
                    themeIndex
                }else{
                    themeIndex=themesList.size-1

                    themeIndex
                }
        ]

    var themeIndex:Int = 0
    val themesList: ArrayList<Int> = ArrayList<Int>(listOf(
        R.drawable.gradient_right_corner,
        R.drawable.gradient2,
        R.drawable.gradient3,
        R.drawable.vibrantgradient
        ))

    var orientation:Int = 0

    var titleTextSize = 24.sp



    fun applySettings(mains: MutableList<View>?, alsoDoRestore:(()->Unit)?=null){

        if(mains!=null){
            mains.forEach { it->
                it.background= ContextCompat.getDrawable(Application.Companion.instance, theme)
            }
        }


        if(alsoDoRestore!=null){
            alsoDoRestore.invoke()
        }

    }


    fun restoreSettings(){
        themeIndex = loadFromJson(
            Application.Companion.instance,"Background Theme",0, File(
                Application.Companion.instance.filesDir, "Settings"
            )
        )

        orientation = loadFromJson(
            Application.Companion.instance,"Orientation",0, File(
                Application.Companion.instance.filesDir, "Settings"
            )
        )




    }
}