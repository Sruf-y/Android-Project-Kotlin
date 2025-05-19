package SongsMain.Variables

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.Playlist.Companion.from
import SongsMain.Classes.Song
import SongsMain.Classes.Song.Companion.from
import SongsMain.Classes.Song.Companion.takeYourPartFromGlobal
import SongsMain.Classes.myExoPlayer
import SongsMain.Tutorial.Application
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
        private var auxlista:Queue<AtomicReference<Playlist>> = LinkedList<AtomicReference<Playlist>>()

        fun reload(){
            auxlista.clear()
            auxlista.addAll(listOf<AtomicReference<Playlist>>(AtomicReference(SongsGlobalVars.RecentlyPlayed),AtomicReference(SongsGlobalVars.MyFavoritesPlaylist),
                AtomicReference(SongsGlobalVars.hiddenSongs),
                AtomicReference(SongsGlobalVars.publicSongs)))
            SongsGlobalVars.userMadePlaylists.forEach {
                auxlista.add(AtomicReference(it))
            }
        }

        fun getListOfRefferences():List<AtomicReference<Playlist>>{
            return auxlista.toList()
        }

        fun getList():List<Playlist>{
            return auxlista.map{p->p.get()}
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
         * Saves the currentlyplaying song AND the playlist it is from. If both exist and are not null
         *
         * NEEDS TO RUN ON MAIN THREAD
         * */

        @OptIn(UnstableApi::class,DelicateCoroutinesApi::class)
        fun saveCurrentlyPlayedSong(){

                val directory = File(Application.instance.filesDir,"SavedLastPlayed")

                if(myExoPlayer.currentPlaylist!=null &&
                    myExoPlayer.currentlyPlayingSong!=null &&
                    myExoPlayer.currentPlaylist!!.get().songsList?.contains(myExoPlayer.currentlyPlayingSong)==true)
                {
                    Functions.saveAsJson(
                        Application.instance,
                        "CurrentlyPlayingSong",
                        myExoPlayer.currentlyPlayingSong!!,
                        directory
                    )

                    Functions.saveAsJson(
                        Application.instance,
                        "CurrentPlaylist",
                        myExoPlayer.currentPlaylist!!.get(),
                        directory
                    )

                    Functions.saveAsJson(
                        Application.instance,
                        "SongPosition",
                        myExoPlayer.getCurrentPosition().toDouble(),
                        directory
                    )
                }

        }

        /**
         * Restores the last playing song if it existed and was in a valid playlist
         *
         * NEEDS TO RUN ON MAIN THREAD
         * */


        @OptIn(UnstableApi::class)
        fun restoreCurrentlyPlayedSong(){
            val directory = File(Application.instance.filesDir,"SavedLastPlayed")
            val currentsong = Functions.loadFromJson(
                Application.instance,
                "CurrentlyPlayingSong",
                Song.emptySong,
                directory
            )

            val currentplaylist = Functions.loadFromJson(
                Application.instance,
                "CurrentPlaylist",
                Playlist.emptyplaylist,
                directory
            )

            var song_position:Double = Functions.loadFromJson(
                Application.instance,
                "SongPosition",
                Double.MIN_VALUE,
                directory
            )





            if(currentsong!=Song.emptySong && currentplaylist!=Playlist.emptyplaylist){



                listOfAllPlaylists.reload()

                val actualPlaylist =
                    listOfAllPlaylists.getListOfRefferences()[listOfAllPlaylists.getList().indexOf(currentplaylist)]


                val playlistToOpen = actualPlaylist.get().from()

                if(playlistToOpen!=null) {

                    try {

                        myExoPlayer.setSong(currentsong, playlistToOpen, startOnPrepared = false)

                        Log.e(Logs.ERRORS.name,"Restored song_position was ${song_position}")
                        myExoPlayer.seekTo(song_position.toLong())




                    }catch (ex: NullPointerException){
                        Log.e(Logs.ERRORS.name,"Null song set when restoring currentlyplaying.",ex)
                    }

                }


            }
        }




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
                        Playlist("Recently Played", ArrayList<Song>(), false)
                    )
                    SongsGlobalVars.MyFavoritesPlaylist = Functions.loadFromJson(
                        Application.instance,
                        "Favorites",
                        Playlist("Favorites", ArrayList<Song>(), true)
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
                                Playlist("Favorites", ArrayList<Song>(), true)
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


                SongsGlobalVars.hiddenSongs.songsList.clear()
                SongsGlobalVars.publicSongs.songsList.clear()

                SongsGlobalVars.allSongs.forEach {
                    if(it.isHidden){

                        SongsGlobalVars.hiddenSongs.songsList.add(it)
                    }
                    else{

                        SongsGlobalVars.publicSongs.songsList.add(it)
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
        fun Song.toggleFavorite(){





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

                    if(publicSongs.songsList?.contains(myExoPlayer.currentlyPlayingSong!!) == true){
                        myExoPlayer.openPlaylist(AtomicReference<Playlist>(publicSongs))
                    }
                    else{
                        myExoPlayer.openPlaylist(AtomicReference<Playlist>(hiddenSongs))
                    }
                }





//                // also update in the current playlist
//                myExoPlayer.currentlyPlayingSong?.from(myExoPlayer.currentPlaylist?.get()?.songsList!!)?.isFavorite =it.isFavorite





                SongsGlobalVars.SongsStorageOperations.SaveSpecifficList.Favorites_List()
            }

            




        }
    }


    var globalDataLoadBuffer_Free = true
    var globalDataSaveBuffer_Free = true


}