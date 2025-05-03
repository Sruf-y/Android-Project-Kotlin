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
    class MakeCurrentMainFragment(val fragment: Class<out Fragment>)

}