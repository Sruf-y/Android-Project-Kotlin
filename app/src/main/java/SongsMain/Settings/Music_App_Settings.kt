package SongsMain.Settings

import Functions.ViewAttributes
import Functions.saveAsJson
import Functions.setInsetsforItems
import SongsMain.Classes.Events
import SongsMain.Tutorial.Application
import SongsMain.bottomSheetFragment
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.GridView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.composepls.R
import de.greenrobot.event.EventBus
import java.io.File

class Music_App_Settings : Fragment(R.layout.fragment_music__app__settings) {

    val bus = EventBus.getDefault()

    lateinit var main: ConstraintLayout
    lateinit var radioGroup: RadioGroup


    lateinit var grid: GridView
    lateinit var gridadaptor: ThemesAdapter

    lateinit var numbertextview: EditText



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main = requireView().findViewById(R.id.main)
        radioGroup=requireView().findViewById(R.id.radiogroup)
        // TODO REMOVE LATER
        numbertextview=requireView().findViewById(R.id.editTextNumber)

        //TODO REMOVE THIS LATER
        numbertextview.setText(MusicAppSettings.titleTextSize.value.toString())
        numbertextview.doOnTextChanged { text,_,_,_->
            if(text?.length == 0){
                Toast.makeText(Application.instance,"Needs a value",Toast.LENGTH_SHORT).show()
            }
            else text.toString().toFloat().let {
                if(it>30f){
                    Toast.makeText(Application.instance,"Maximum is 30",Toast.LENGTH_SHORT).show()
                } else if(it<5f){
                    Toast.makeText(Application.instance,"Minimum is 5",Toast.LENGTH_SHORT).show()
                } else{
                    MusicAppSettings.titleTextSize=it.sp
                }
            }
        }


        // TODO REMOVE LATER
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom+ime.bottom)
            insets
        }


        // TODO REMOVE LATER
        Functions.setAnimationForKeyboard(main)


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

        grid = requireView().findViewById(R.id.themesGrid)
        gridadaptor = ThemesAdapter(
            requireContext(),
            MusicAppSettings.themesList,
            {drawableRes,position->

                // set the position
                MusicAppSettings.themeIndex= position
                // set the current page's background
                ViewAttributes(main).Background().Set(requireContext(), drawableRes)

                saveAsJson(
                    Application.Companion.instance, "Background Theme", MusicAppSettings.themeIndex,
                    File(requireContext().filesDir, "Settings")
                )

                bus.post(Events.SettingsWereChanged())

            })
        grid.adapter=gridadaptor

        gridadaptor.notifyDataSetChanged()




        radioGroup.check(radioGroup.get(MusicAppSettings.orientation).id)

        setInsetsforItems(mutableListOf(main))







        val backbutton = requireActivity().onBackPressedDispatcher
        //back button
        backbutton.addCallback(viewLifecycleOwner) {
            // Handle the back press

            saveAsJson(
                Application.Companion.instance, "Background Theme", MusicAppSettings.themeIndex,
                File(requireContext().filesDir, "Settings")
            )

            saveAsJson(
                Application.Companion.instance, "Orientation", MusicAppSettings.orientation,
                File(requireContext().filesDir, "Settings")
            )



            bus.post(Events.SettingsWereChanged())


            isEnabled=false

            backbutton.onBackPressed()


        }






    }

    override fun onPause() {

        Functions.saveAsJson(Application.instance,"TitleSize", MusicAppSettings.titleTextSize)

        super.onPause()
    }


}