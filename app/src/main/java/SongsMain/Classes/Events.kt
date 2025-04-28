package SongsMain.Classes

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

}