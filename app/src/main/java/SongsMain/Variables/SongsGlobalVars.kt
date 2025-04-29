package SongsMain.Variables

import SongsMain.Classes.Playlist
import SongsMain.Classes.Song
import SongsMain.Tutorial.Application
import android.content.Context
import java.io.File

object SongsGlobalVars {
    val CHANNEL_ID = "CHANNEL_ID"
    val CHANNEL_NAME= "CHANNEL_NAME"

    fun musicDirectory(context:Context?=null):File{
        return File(Application.instance.filesDir,"MusicDir")
    }



    var playingQueue:ArrayList<Song> = ArrayList<Song>()

    var playlistsList: ArrayList<Playlist> = ArrayList<Playlist>()

    var MyFavoritesPlaylist: Playlist = Playlist("Favorites", ArrayList<Song>(),false)
    var RecentlyPlayed: Playlist = Playlist("Recently Played",ArrayList<Song>(),false)




    var allSongs: ArrayList<Song> = ArrayList<Song>()

    var hiddenSongs: Playlist = Playlist("Hidden Songs",ArrayList<Song>(),false)
    var publicSongs: Playlist = Playlist("Public Songs",ArrayList<Song>(),false)






    var refreshBufferIsFree = true
    var saveBufferIsFree = true


}