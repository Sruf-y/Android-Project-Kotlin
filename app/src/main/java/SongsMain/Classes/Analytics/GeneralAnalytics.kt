package SongsMain.Classes.Analytics

import SongsMain.Tutorial.Application
import kotlinx.serialization.Serializable
import java.io.File

object GeneralAnalytics {
    var songs_searched = 0
    var playlists_created = 0

    @Serializable
    private class myData(val sonsSearched:Int,val playlistsCreated:Int) {}

    private val parentDirectory =File(Application.instance.filesDir,"Analytics")

    fun saveAnalytics(){
        Functions.saveAsJson(
            Application.instance,
            "Generic data",
            myData(songs_searched, playlists_created),
            parentDirectory)
    }

    fun restoreAnalytics(){

        val restored = Functions.loadFromJson(
            Application.instance,
            "Generic data",
            myData(songs_searched, playlists_created),
            parentDirectory
        )

        songs_searched=restored.sonsSearched
        playlists_created=restored.playlistsCreated
    }


}