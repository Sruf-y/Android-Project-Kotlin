package SongsMain.Variables

import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.Song
import SongsMain.Classes.Song.Companion.from
import SongsMain.Classes.Song.Companion.takeYourPartFromGlobal
import SongsMain.Classes.myExoPlayer
import SongsMain.Classes.myMediaPlayer
import SongsMain.Tutorial.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicReference

object SongsGlobalVars {
    val CHANNEL_ID = "CHANNEL_ID_botofan"
    val CHANNEL_NAME= "CHANNEL_NAME_botofan"

    fun musicDirectory(context:Context?=null):File{
        return File(Application.instance.filesDir,"MusicDir")
    }

    private val bus = EventBus.getDefault()

    // i don't plan on doing anything with this honestly
    var playingQueue:ArrayList<Song> = ArrayList<Song>()


    object listOfAllPlaylists{
        private var auxlista:Queue<Playlist> = LinkedList<Playlist>()

        fun reload(){
            auxlista.clear()
            auxlista.addAll(listOf<Playlist>(SongsGlobalVars.RecentlyPlayed,SongsGlobalVars.MyFavoritesPlaylist,
                SongsGlobalVars.hiddenSongs,
                SongsGlobalVars.publicSongs))

            auxlista.addAll(SongsGlobalVars.userMadePlaylists)
        }

        fun get():List<Playlist>{
            return auxlista.toList()
        }
    }

    var userMadePlaylists: ArrayList<Playlist> = ArrayList<Playlist>()


    var MyFavoritesPlaylist: Playlist = Playlist("Favorites", ArrayList<Song>(),false,null,true)
    var RecentlyPlayed: Playlist = Playlist("Recently Played",ArrayList<Song>(),false,null,false)




    var allSongs: ArrayList<Song> = ArrayList<Song>()

    var hiddenSongs: Playlist = Playlist("Hidden Songs",ArrayList<Song>(),false,null,true)
    var publicSongs: Playlist = Playlist("Public Songs",ArrayList<Song>(),false,null,false)

    object SongsStorageOperations{
         var saveBuffer_free=true
         var loadBuffer_free=true





        /**
         * Reloads from internal memory ONLY "SongsGlobalValues.alllist". It does NOT distribute NOR refresh any other playlists/lists
         * */
        suspend fun refreshGlobalSongList():Boolean{
            return withContext(Dispatchers.IO) {

                while(!loadBuffer_free){delay(50)}

                if(loadBuffer_free) {
                    loadBuffer_free=false
                SongsGlobalVars.allSongs.clear()            // am facut load la lista de songs. Fac load si la playlists, si split pe public si hidden songs.
                SongsGlobalVars.allSongs.addAll(Functions.loadFromJson(Application.instance, "GlobalSongs", SongsGlobalVars.allSongs))
                    loadBuffer_free=true
                }

                true
            }
        }

        /**
         * Reloads from internal memory all lists except the pre-existing "SongsGlobalValues.alllist".
         *
         * It does NOT also redistribute from the global list. Call redistributeLists() for that.
         * */
        suspend fun refreshSongLists():Boolean{
            return withContext(Dispatchers.IO) {

                while(!loadBuffer_free){delay(50)}
                if(loadBuffer_free) {
                    loadBuffer_free=false
                    SongsGlobalVars.userMadePlaylists.clear()
                    SongsGlobalVars.userMadePlaylists.addAll(
                        Functions.loadFromJson(
                            Application.instance,
                            "PlaylistsList",
                            ArrayList<Playlist>()
                        )
                    )

                    SongsGlobalVars.userMadePlaylists.forEach {
                        it.songsList.takeYourPartFromGlobal()
                    }

                    SongsGlobalVars.RecentlyPlayed = Functions.loadFromJson(
                        Application.instance,
                        "Recently Played",
                        Playlist("Recently Played", null, false)
                    )
                    SongsGlobalVars.MyFavoritesPlaylist = Functions.loadFromJson(
                        Application.instance,
                        "Favorites",
                        Playlist("Favorites", null, true)
                    )

                    SongsGlobalVars.playingQueue.clear()
                    SongsGlobalVars.playingQueue.addAll(
                        Functions.loadFromJson(
                            Application.instance,
                            "Playing Queue",
                            ArrayList<Song>()
                        )
                    )

                    SongsGlobalVars.hiddenSongs.songsList = ArrayList<Song>()
                    SongsGlobalVars.publicSongs.songsList = ArrayList<Song>()
                    SongsGlobalVars.allSongs.forEach {
                        if (it.isHidden) {
                            SongsGlobalVars.hiddenSongs.add(it)
                        } else {
                            SongsGlobalVars.publicSongs.add(it)
                        }
                    }

                    listOfAllPlaylists.reload()

                    loadBuffer_free=true
                }

                true
            }
        }

        object ReloadSpecifficList{
            fun List_Of_Playlists(){

                CoroutineScope(Dispatchers.IO).launch {
                    return@launch withContext(Dispatchers.IO) {
                        while (!loadBuffer_free) {
                            delay(50)
                        }
                        if (loadBuffer_free) {
                            loadBuffer_free = false

                            SongsGlobalVars.userMadePlaylists.clear()
                            SongsGlobalVars.userMadePlaylists.addAll(
                                Functions.loadFromJson(
                                    Application.instance,
                                    "PlaylistsList",
                                    ArrayList<Playlist>()
                                )
                            )
                            SongsGlobalVars.userMadePlaylists.forEach {
                                it.songsList.takeYourPartFromGlobal()
                            }

                            loadBuffer_free=true
                        }
                    }
                }
            }

            fun Favorites_List(){
                CoroutineScope(Dispatchers.IO).launch {
                    return@launch withContext(Dispatchers.IO) {
                        while (!loadBuffer_free) {
                            delay(50)
                        }
                        if (loadBuffer_free) {
                            loadBuffer_free = false

                            SongsGlobalVars.MyFavoritesPlaylist = Functions.loadFromJson(
                                Application.instance,
                                "Favorites",
                                Playlist("Favorites", null, true)
                            )


                            loadBuffer_free = true
                        }
                    }
                }
            }
        }

        object SaveSpecifficList{
            fun List_Of_Playlists(){
                CoroutineScope(Dispatchers.IO).launch {
                    return@launch withContext(Dispatchers.IO) {

                        while (!saveBuffer_free) {
                            delay(50)
                        }
                        if (saveBuffer_free) {
                            saveBuffer_free = false

                            Functions.saveAsJson(
                                Application.instance,
                                "PlaylistsList",
                                SongsGlobalVars.userMadePlaylists
                            )


                            saveBuffer_free = true
                        }
                    }
                }
            }

            fun Favorites_List(){
                CoroutineScope(Dispatchers.IO).launch {
                    return@launch withContext(Dispatchers.IO) {

                        while (!saveBuffer_free) {
                            delay(50)
                        }
                        if (saveBuffer_free) {
                            saveBuffer_free = false


                            Functions.saveAsJson(
                                Application.instance,
                                "Favorites",
                                SongsGlobalVars.MyFavoritesPlaylist
                            )

                            saveBuffer_free = true
                        }
                    }
                }
            }
        }











        /**
         * Redistributes songs from the pre-existing "SongsGlobalValues.alllist" into every other list and playlist
         * */
        suspend fun redistributeLists():Boolean{
            return withContext(Dispatchers.IO) {

                SongsGlobalVars.playingQueue.takeYourPartFromGlobal()
                SongsGlobalVars.RecentlyPlayed.songsList.takeYourPartFromGlobal()
                SongsGlobalVars.MyFavoritesPlaylist.songsList.takeYourPartFromGlobal()
                SongsGlobalVars.userMadePlaylists.forEach {
                    it.songsList.takeYourPartFromGlobal()
                }

                SongsGlobalVars.allSongs.forEach {
                    if(it.isHidden){
                        SongsGlobalVars.hiddenSongs.songsList?.add(it)
                    }
                    else{
                        SongsGlobalVars.publicSongs.songsList?.add(it)
                    }
                }


                true
            }
        }


        /**
         * Saves all song lists (except public and private ones, those are distributed at runtime)
         * */
        suspend fun saveSongLists():Boolean{
            return withContext(Dispatchers.IO) {

                while(!saveBuffer_free){
                    delay(50)
                }
                if(saveBuffer_free) {
                    saveBuffer_free=false
                    Functions.saveAsJson(Application.instance, "GlobalSongs", SongsGlobalVars.allSongs)
                    Functions.saveAsJson(
                        Application.instance,
                        "PlaylistsList",
                        SongsGlobalVars.userMadePlaylists
                    )
                    Functions.saveAsJson(
                        Application.instance,
                        "Recently Played",
                        SongsGlobalVars.RecentlyPlayed
                    )
                    Functions.saveAsJson(
                        Application.instance,
                        "Favorites",
                        SongsGlobalVars.MyFavoritesPlaylist
                    )
                    Functions.saveAsJson(
                        Application.instance,
                        "Playing Queue",
                        SongsGlobalVars.playingQueue
                    )
                    saveBuffer_free=true
                }
                true
            }
        }
    }

    object SongsOperations{
        @OptIn(UnstableApi::class)
        fun Song.setFavorite(favorite:Boolean){





            this.from(allSongs)?.let {
                if(it.isFavorite){
                    it.isFavorite=false

                    if(MyFavoritesPlaylist.songsList==null){
                        MyFavoritesPlaylist.songsList= ArrayList<Song>()
                    }
                    // este favorit, verific daca este in lista de favorite si il scot daca exista
                    if(MyFavoritesPlaylist.songsList?.contains(it)?:false){
                        MyFavoritesPlaylist.songsList?.remove(it)
                    }
                }
                else{
                    it.isFavorite=true
                    //verific daca nu este in lista de favorite, daca nu, il adaug.
                    if(!(MyFavoritesPlaylist.songsList?.contains(it)?:false)){
                        if(MyFavoritesPlaylist.songsList==null){
                            MyFavoritesPlaylist.songsList= ArrayList<Song>()
                        }

                        MyFavoritesPlaylist.songsList?.add(it)
                    }
                }
                bus.post(Events.PlaylistWasChanged(MyFavoritesPlaylist))

                if(myExoPlayer.currentPlaylist?.get()==MyFavoritesPlaylist){

                    // in case i am listening to the songs in the favorites playlist,
                    // removing the song puts me in no playlist. Handling this

                    if(publicSongs.songsList?.contains(myExoPlayer.currentlyPlayingSong!!)?:false){
                        myExoPlayer.openPlaylist(AtomicReference<Playlist>(publicSongs))
                    }
                    else{
                        myExoPlayer.openPlaylist(AtomicReference<Playlist>(hiddenSongs))
                    }
                }

                SongsGlobalVars.SongsStorageOperations.SaveSpecifficList.Favorites_List()
            }

            

//            myExoPlayer.currentPlaylist?.get().let {
//                if(it!=null){
//                    if(it.songsList==null){
//                        it.songsList= ArrayList<Song>()
//                    }
//                    this.from(it.songsList!!).isFavorite=
//                }
//            }


        }
    }


    var globalDataLoadBuffer_Free = true
    var globalDataSaveBuffer_Free = true


}