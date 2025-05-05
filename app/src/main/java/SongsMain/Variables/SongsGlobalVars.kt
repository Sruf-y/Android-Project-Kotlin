package SongsMain.Variables

import SongsMain.Classes.Playlist
import SongsMain.Classes.Song
import SongsMain.Tutorial.Application
import android.content.Context
import com.example.composepls.R
import java.io.File

object SongsGlobalVars {
    val CHANNEL_ID = "CHANNEL_ID"
    val CHANNEL_NAME= "CHANNEL_NAME"

    fun musicDirectory(context:Context?=null):File{
        return File(Application.instance.filesDir,"MusicDir")
    }



    var playingQueue:ArrayList<Song> = ArrayList<Song>()

    var playlistsList: ArrayList<Playlist> = ArrayList<Playlist>()

    var MyFavoritesPlaylist: Playlist = Playlist("Favorites", ArrayList<Song>(),false,R.drawable.blank_gray_musical_note)
    var RecentlyPlayed: Playlist = Playlist("Recently Played",ArrayList<Song>(),false,R.drawable.blank_gray_musical_note)




    var allSongs: ArrayList<Song> = ArrayList<Song>()

    var hiddenSongs: Playlist = Playlist("Hidden Songs",ArrayList<Song>(),false,R.drawable.original_doge_meme)
    var publicSongs: Playlist = Playlist("Public Songs",ArrayList<Song>(),false,R.drawable.favorite_heart_button)






    var refreshBufferIsFree = true
    var saveBufferIsFree = true


}