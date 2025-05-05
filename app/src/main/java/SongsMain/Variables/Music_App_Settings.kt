package SongsMain.Variables

import Functions.ViewAttributes
import Functions.saveAsJson
import Functions.setInsetsforItems
import SongsMain.Classes.Events
import SongsMain.Tutorial.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.composepls.R
import de.greenrobot.event.EventBus
import java.io.File

class Music_App_Settings : Fragment(R.layout.fragment_music__app__settings) {

    val bus = EventBus.getDefault()

    lateinit var main: ConstraintLayout
    lateinit var radioGroup: RadioGroup


    lateinit var theme1: ImageView
    lateinit var theme2: ImageView
    lateinit var theme3: ImageView




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main = requireView().findViewById(R.id.main)
        radioGroup=requireView().findViewById(R.id.radiogroup)

        MusicAppSettings.applySettings(mutableListOf(main))

        radioGroup.forEach { radioButton->
            //(radioButton as RadioButton).buttonDrawable= MusicAppSettings.getDarkModeRadioButton()
        }


        radioGroup.setOnCheckedChangeListener(object: RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
                when(id){
                    R.id.radioButton->{
                        MusicAppSettings.orientation=0
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT}
                    R.id.radioButton2->{
                        MusicAppSettings.orientation=1
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    R.id.radioButton3->{
                        MusicAppSettings.orientation=2
                        requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                }
            }
        })



        radioGroup.check(radioGroup.get(MusicAppSettings.orientation).id)

        setInsetsforItems(mutableListOf(main))

        theme1=requireView().findViewById(R.id.theme1)
        theme2=requireView().findViewById(R.id.theme2)
        theme3=requireView().findViewById(R.id.theme3)

        theme1.setOnClickListener{
            MusicAppSettings.theme= R.drawable.gradient_right_corner
            ViewAttributes(main).Background().Set(requireContext(), R.drawable.gradient_right_corner)
            saveAsJson(
                Application.Companion.instance,"Background Theme", MusicAppSettings.theme,
                File(requireContext().filesDir, "Settings")
            )
            bus.post(Events.SettingsWereChanged())
        }
        theme2.setOnClickListener{
            MusicAppSettings.theme= R.drawable.gradient2
            ViewAttributes(main).Background().Set(requireContext(), R.drawable.gradient2)
            saveAsJson(
                Application.Companion.instance,"Background Theme", MusicAppSettings.theme,
                File(requireContext().filesDir, "Settings")
            )
            bus.post(Events.SettingsWereChanged())
        }
        theme3.setOnClickListener{
            MusicAppSettings.theme= R.drawable.gradient3
            ViewAttributes(main).Background().Set(requireContext(), R.drawable.gradient3)
            saveAsJson(
                Application.Companion.instance,"Background Theme", MusicAppSettings.theme,
                File(requireContext().filesDir, "Settings")
            )
            bus.post(Events.SettingsWereChanged())
        }


        val backbutton = requireActivity().onBackPressedDispatcher
        //back button
        backbutton.addCallback(viewLifecycleOwner) {
            // Handle the back press

            saveAsJson(
                Application.Companion.instance,"Background Theme", MusicAppSettings.theme,
                File(requireContext().filesDir, "Settings")
            )

            saveAsJson(
                Application.Companion.instance,"Orientation", MusicAppSettings.orientation,
                File(requireContext().filesDir, "Settings")
            )



            bus.post(Events.SettingsWereChanged())


            isEnabled=false

            backbutton.onBackPressed()
        }






    }


}