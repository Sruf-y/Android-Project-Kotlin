package SongsMain.Classes

import androidx.fragment.app.Fragment

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

    object InPlaylistEvents{
        class NotifyAdded()
        class NotifyDeleted(val playlistToDelete: Playlist?)
        class NotifyChanged(val previousTitle:String,val playlistThatChanged: Playlist?)
    }


    //class ShowSetting_BottomDialog(val bottomDialog: BottomSheetDialogFragment)

    class PlaylistWasChanged(val playlist:Playlist)
}