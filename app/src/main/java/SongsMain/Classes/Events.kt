package SongsMain.Classes

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

object Events {

    class GlobalDataWasUpdated()
    class RequestGlobalDataUpdate()

    class SongWasChanged(val lastSong: Song?, val currentSong: Song?){
    }
    class SongWasPaused()
    class SongWasStarted()
    class SongWasReset()
    class SongWasStopped()
    class SongWas_UsedSeek()

    class SearchButtonPressed()
    class ReturnToMainBase()
    class SettingsWereChanged()
    class MakeCurrentMainFragment(val fragment: Fragment)
    class MakeCurrent_BottomSheet_Fragment(val fragment: Fragment)


    //class ShowSetting_BottomDialog(val bottomDialog: BottomSheetDialogFragment)

}