package SongsMain.BottomSheetDialogs

import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Variables.MusicAppSettings
import SongsMain.Variables.SongsGlobalVars
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.composepls.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.greenrobot.event.EventBus


private const val playlistIn_title = "playlist"


class Remove_Playlist : BottomSheetDialogFragment(R.layout.fragment_remove__playlist) {

    val bus = EventBus.getDefault()

    lateinit var cancelButton: Button
    lateinit var confirmButton:Button
    lateinit var displayTitleTextView: TextView

    lateinit var main: LinearLayout
    lateinit var main2: ConstraintLayout

    private var playlist: Playlist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlist = it.getParcelable(playlistIn_title, Playlist::class.java)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main=requireView().findViewById(R.id.main)
        main2=requireView().findViewById(R.id.main2)

        displayTitleTextView=requireView().findViewById(R.id.textviewfortitle)

        cancelButton=requireView().findViewById(R.id.cancelButton)
        confirmButton=requireView().findViewById(R.id.deleteButton)

        if(playlist!=null){
            displayTitleTextView.text=playlist!!.title+"?"
        }

        MusicAppSettings.applySettings(mutableListOf(main2))


    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout

            bottomSheet.setBackgroundColor(resources.getColor(R.color.transparent))


            //WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)


            val window = dialog.window


            val bottomSheetView =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            window?.let { it ->
                Log.i("WTF", bottomSheetView?.parent?.javaClass?.name.toString())

                WindowCompat.setDecorFitsSystemWindows(it, false)


//                ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
//                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() )
//                    val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
//                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom+ime.bottom)
//                    insets
//                }
                ViewCompat.setOnApplyWindowInsetsListener(bottomSheetView?.parent as View) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom + ime.bottom
                    )
                    insets
                }


                Functions.setAnimationForKeyboard(bottomSheet)


                //TODO nothing, just green //////////////////////////////////////////////////////////


                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                confirmButton.setOnClickListener {
                    if(playlist!=null){

                        SongsGlobalVars.userMadePlaylists.remove(playlist)
                        SongsGlobalVars.listOfAllPlaylists.reload()

                        SongsGlobalVars.SongsStorageOperations.SaveSpecifficList.List_Of_Playlists()

                        bus.post(Events.PlaylistEvents.NotifyDeleted(playlist))
                        dialog.dismiss()
                    }
                }

            }
        }

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return dialog
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Remove_Playlist.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Playlist) =
            Remove_Playlist().apply {
                arguments = Bundle().apply {
                    putParcelable(playlistIn_title, param1)
                }
            }
    }
}