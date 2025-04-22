package SongsMain.Classes

object Events {

    class GlobalDataWasUpdated()
    class RequestGlobalDataUpdate()

    class SongWasChanged(val currentSong: Song?, val nextSong: Song?){
    }
    class SongWasPaused()
    class SongWasStarted()
    class SongWasReset()
    class SongWasStopped()
}