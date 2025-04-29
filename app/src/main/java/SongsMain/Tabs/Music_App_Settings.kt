package SongsMain.Tabs

import DataClasses_Ojects.ViewAttributes
import SongsMain.Classes.Events
import SongsMain.Tutorial.Application
import SongsMain.Variables.MusicAppSettings
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.composepls.R
import com.google.android.material.button.MaterialButton
import de.greenrobot.event.EventBus
import java.io.File

class Music_App_Settings : Fragment(R.layout.fragment_music__app__settings) {

    val bus = EventBus.getDefault()

    lateinit var main: ConstraintLayout
    lateinit var radioGroup: RadioGroup


    lateinit var theme1:ImageView
    lateinit var theme2:ImageView
    lateinit var theme3:ImageView




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main = requireView().findViewById(R.id.main)
        radioGroup=requireView().findViewById(R.id.radiogroup)

        applySettings()

        radioGroup.setOnCheckedChangeListener(object:RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
                when(id){
                    R.id.radioButton->{
                        MusicAppSettings.orientation=0
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
                    R.id.radioButton2->{
                        MusicAppSettings.orientation=1
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    R.id.radioButton3->{
                        MusicAppSettings.orientation=2
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            }
        })



        radioGroup.check(radioGroup.get(MusicAppSettings.orientation).id)

        Functions.setInsetsforItems(mutableListOf(main))

        theme1=requireView().findViewById(R.id.theme1)
        theme2=requireView().findViewById(R.id.theme2)
        theme3=requireView().findViewById(R.id.theme3)

        theme1.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient_right_corner
            Functions.ViewAttributes(main).Background().Set(requireContext(),R.drawable.gradient_right_corner)
            Functions.saveAsJson(Application.instance,"Background Theme", MusicAppSettings.theme,File(requireContext().filesDir,"Settings"))
        }
        theme2.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient2
            Functions.ViewAttributes(main).Background().Set(requireContext(),R.drawable.gradient2)
            Functions.saveAsJson(Application.instance,"Background Theme", MusicAppSettings.theme,File(requireContext().filesDir,"Settings"))
        }
        theme3.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient3
            Functions.ViewAttributes(main).Background().Set(requireContext(),R.drawable.gradient3)
            Functions.saveAsJson(Application.instance,"Background Theme", MusicAppSettings.theme,File(requireContext().filesDir,"Settings"))
        }


        val backbutton = requireActivity().onBackPressedDispatcher
        //back button
        backbutton.addCallback(viewLifecycleOwner) {
            // Handle the back press

            Functions.saveAsJson(Application.instance,"Background Theme", MusicAppSettings.theme,File(requireContext().filesDir,"Settings"))

            Functions.saveAsJson(Application.instance,"Orientation", MusicAppSettings.orientation,File(requireContext().filesDir,"Settings"))


            bus.post(Events.ReturnToMainBase())
            //isEnabled=false

        }
    }

    fun applySettings(){
        main.background= ContextCompat.getDrawable(requireContext(),MusicAppSettings.theme)
    }
}