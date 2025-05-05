package SongsMain.BottomSheetDialogs

import Functions.ViewAttributes
import SongsMain.Variables.MusicAppSettings
import SongsMain.Variables.SongsGlobalVars
import SongsMain.bottomSheetFragment
import Utilities.Utils.Companion.dP
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.unit.lerp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.example.composepls.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.greenrobot.event.EventBus
import kotlin.math.max


private const val ARG_PARAM1 = "title"


class Add_Playlist : BottomSheetDialogFragment(R.layout.fragment_add__playlist) {

    val bus= EventBus.getDefault()
    private var title: String? = null

    lateinit var main: LinearLayout
    lateinit var main2: ConstraintLayout

    lateinit var charcounter: TextView
    lateinit var nextToCharCounter: TextView
    lateinit var editPlaylistName: EditText
    lateinit var cancelButton: Button
    lateinit var confirmButton:Button
    val maxchar=40

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        arguments?.let {
            title = it.getString(ARG_PARAM1)
        }



    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main=requireView().findViewById(R.id.main)
        main2=requireView().findViewById(R.id.main2)
        charcounter=requireView().findViewById(R.id.charcounter)
        nextToCharCounter=requireView().findViewById(R.id.nextToCharCounter)
        editPlaylistName=requireView().findViewById(R.id.editPlaylistName)
        cancelButton=requireView().findViewById(R.id.cancelButton)
        confirmButton=requireView().findViewById(R.id.confirmButton)


        //Functions.setInsetsforItems(mutableListOf(main))

        MusicAppSettings.applySettings(mutableListOf(main2))


        nextToCharCounter.text="/${maxchar}"
        charcounter.text="0"

        editPlaylistName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Called *after* the text is changed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Called *before* the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Called *as* the text is changing

                charcounter.text=s?.length.toString()
                if((s?.length ?: 0) > maxchar){
                    // if name is invalid, too long
                    charcounter.setTextColor(resources.getColor(R.color.red))


                }
                else{
                    // if name is valid
                    charcounter.setTextColor(resources.getColor(R.color.white))


                }

            }
        })





    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog


        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout

            bottomSheet.setBackgroundColor(resources.getColor(R.color.transparent))


            //WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)



            val window = dialog.window




            val bottomSheetView = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            window?.let{it->
                Log.i("WTF",bottomSheetView?.parent?.javaClass?.name.toString())

                WindowCompat.setDecorFitsSystemWindows(it, false)


//                ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
//                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() )
//                    val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
//                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom+ime.bottom)
//                    insets
//                }
                ViewCompat.setOnApplyWindowInsetsListener(bottomSheetView?.parent as View) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() )
                    val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom+ime.bottom)
                    insets
                }


                Functions.setAnimationForKeyboard(bottomSheet)

            }
        }





        // out of everything i've tried, this is the only thing that consistently makes the BottomDialogFragment snap to the top of the keyboard.
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)



        return dialog
    }










    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment Add_Playlist.
         */
        @JvmStatic
        fun newInstance(title: String) =
            Add_Playlist().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, title)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        bus.apply {
            if(isRegistered(this))
                unregister(this)
        }
    }
}