package SongsMain.Tabs

import DataClasses_Ojects.ViewAttributes
import SongsMain.Classes.Events
import SongsMain.Variables.MusicAppSettings
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import com.example.composepls.R
import de.greenrobot.event.EventBus

class Music_App_Settings : Fragment(R.layout.fragment_music__app__settings) {

    val bus = EventBus.getDefault()

    lateinit var theme1:ImageView
    lateinit var theme2:ImageView
    lateinit var theme3:ImageView



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme1=requireView().findViewById(R.id.theme1)
        theme2=requireView().findViewById(R.id.theme2)
        theme3=requireView().findViewById(R.id.theme3)

        theme1.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient_right_corner
        }
        theme2.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient2
        }
        theme3.setOnClickListener{
            MusicAppSettings.theme=R.drawable.gradient3
        }


        val backbutton = requireActivity().onBackPressedDispatcher
        //back button
        backbutton.addCallback(viewLifecycleOwner) {
            // Handle the back press


            bus.post(Events.ReturnToMainBase())
            //isEnabled=false

        }
    }
}