package SongsMain.Classes

import android.content.Context
import java.io.File

object SongsGlobalVars {
    val CHANNEL_ID = "CHANNEL_ID"
    val CHANNEL_NAME= "CHANNEL_NAME"

    fun musicDirectory(context:Context):File{
        return File(context.filesDir,"MusicDir")
    }


    var playingQueue:ArrayList<Song> = ArrayList<Song>()

    var playlists: ArrayList<Playlist> = ArrayList<Playlist>()
    var MyFavoritesPlaylist: Playlist? = null
    var RecentlyAddedPlaylist: Playlist?=null
    var RecentlyPlayed: Playlist?=null

    var allSongs: ArrayList<Song> = ArrayList<Song>()

    var hiddenSongs: ArrayList<Song> = ArrayList<Song>()
    var publicSongs: ArrayList<Song> = ArrayList<Song>()




}