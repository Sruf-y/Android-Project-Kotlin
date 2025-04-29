package SongsMain

import DataClasses_Ojects.Logs
import Functions.AskForPermissionsAtStart
import Functions.Images.decodeSampledBitmapFromUri
import Functions.concatenateWith
import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.Song
import SongsMain.Classes.Song.Companion.takeYourPartFromGlobal
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import SongsMain.Tabs.Music_App_Settings
import SongsMain.Tutorial.Application
import SongsMain.Tutorial.MusicPlayerService
import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.composepls.R
import com.google.android.material.navigation.NavigationView
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime


class OpenDrawerEvent()




class SongMain_Activity : AppCompatActivity(){



    val bus:EventBus = EventBus.getDefault();
    var saveBuffer_free=true
    var restoreBuffer_free=true

    lateinit var fragmentContainer: FragmentContainerView
    lateinit var drawer: DrawerLayout

     companion object ActiveTracker {
        var isRunningAnywhere: Boolean = true
        var isPaused: Boolean=false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setContentView(R.layout.activity_songs_main_activity)

        AskForPermissionsAtStart( this,GlobalValues.System.RequiredPermissions.subList(0,4))

        SongMain_Activity.ActiveTracker.isRunningAnywhere=true
        SongMain_Activity.ActiveTracker.isPaused=false







        fragmentContainer=findViewById(R.id.fragmentContainerView)


        if (savedInstanceState == null) {
            Log.i("TESTS","(Main_Activity) Activity set the initial fragment! +${LocalTime.now()}")

            makeCurrentFragment(fragmentContainer, SongsMain.SongsMain_Base::class.java)

        }



        // edge-to-edge //////////////////////////

        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))
        drawer = findViewById(R.id.drawerLayout)

        enableEdgeToEdge(myStatusBarStyle, myNavigationBarStyle)

        val navView: NavigationView = requireViewById(R.id.navView)


        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            navView.fitsSystemWindows = true
            fragmentContainer.fitsSystemWindows = true
        } else {
            navView.fitsSystemWindows = false
            fragmentContainer.fitsSystemWindows = false
        }


        ViewCompat.setOnApplyWindowInsetsListener(navView) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.navigationBars() + WindowInsetsCompat.Type.statusBars())

            v.updatePaddingRelative(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val mainCoord: CoordinatorLayout = findViewById(R.id.mainCoord)

        //////////////////////////////////////////////


        // onclick listeners for buttons, in the navView drawer. Currently just settings
        navView.setNavigationItemSelectedListener(object:NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val id  = item.itemId;

                when(id){
                    R.id.nav_settings->{
                        makeCurrentFragment(fragmentContainer,Music_App_Settings::class.java)
                        return true
                    }
                }
                drawer.close()

                return true
            }
        })




        // should be getting initialized in the service launcher
        //myMediaPlayer.initializeMediaPlayer(this)

        bus.register(this)


        myMediaPlayer.initializeMediaPlayer()

        if(savedInstanceState==null){
            doStartDataLoad()
        }


    }

    fun onEvent(event:Events.ReturnToMainBase){
        makeCurrentFragment(fragmentContainer, SongsMain.SongsMain_Base::class.java)

    }

    fun onEvent(event:Events.SearchButtonPressed){
        makeCurrentFragment(fragmentContainer, SongsMain.search::class.java)
    }


    fun checkBackPressedDispatcherCondition(): Boolean{
        if(fragmentContainer.getFragment<Fragment>()==SongsMain.SongsMain_Base::class.java){
            return true
        }
        else
            return false
    }

    fun onEvent( event: OpenDrawerEvent){
        drawer.open()
    }

    lateinit var serviceIntent:Intent

    fun onEvent(event:Events.RequestGlobalDataUpdate){

        CoroutineScope(Dispatchers.Main).launch {
            SongsGlobalVars.allSongs.clear()
            SongsGlobalVars.allSongs.addAll(doSongsQuery())
            bus.post(Events.GlobalDataWasUpdated())
        }

    }

    fun onEvent( event: Events.SongWasStarted){
        startMusicService()
    }


    fun startMusicService(){
        if(!MusicPlayerService.isServiceRunning())
        {
            serviceIntent = Intent(this, MusicPlayerService::class.java)

            startForegroundService(serviceIntent)

            bus.post(Events.SongWasChanged(null,null))
        }
    }

    






    private fun makeCurrentFragment(container: FragmentContainerView, fragmentClass: Class<out Fragment>) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(container.id, fragmentClass, null, fragmentClass.name)
        }
    }

    override fun onDestroy() {
        Log.i(Logs.LIFECYCLE.toString(),"(Main_Activity) MusicPlayer activity destroyed")


        bus.unregister(this)

        SongMain_Activity.ActiveTracker.isRunningAnywhere=false
        SongMain_Activity.ActiveTracker.isPaused=false

        CoroutineScope(Dispatchers.Main).launch {
            if(SongsGlobalVars.saveBufferIsFree) {
                SongsGlobalVars.saveBufferIsFree=false
                val savedSuccesfully = saveSongLists()
                SongsGlobalVars.saveBufferIsFree=true
            }

        }

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        SongMain_Activity.ActiveTracker.isPaused=false
    }



    fun doStartDataLoad(){
        if(SongsGlobalVars.refreshBufferIsFree) {
            SongsGlobalVars.refreshBufferIsFree=false
            lifecycleScope.launch {
                val songsLoadedSuccesfully = refreshGlobalSongList()

                val playlistsLoadedSuccesfully = refreshSongLists()

                var songsList: ArrayList<Song> = ArrayList<Song>()
                CoroutineScope(Dispatchers.Main).launch {
                    // fac un query light la inceputul aplicatiei, verific daca datele stocate sunt la fel ca cele din query. Daca da, continui. Daca nu, fac query heavy
                    // Dupa ambele,la final, fac request ca datele din restul listelor si recyclelor sa fie updatate

                    songsList = doSongsQuery(false)
                    Log.i(
                        "TESTS",
                        "(Main_Activity) AllSongs list has a size of ${SongsGlobalVars.allSongs.size}"
                    )

                }.invokeOnCompletion {

                    // get the list of items that are not in concordance with the light query and handle them

                    val listInNeedOfUpdates =
                        Functions.differencesBetweenArrays(songsList, SongsGlobalVars.allSongs)
                            .apply {

                                if (this.isNotEmpty()) {

                                    Log.i(
                                        "TESTS",
                                        "(Main_Activity) List in need of udpates of size " + this.size.toString()
                                    )
                                    // exista elemente in neconcordata, fac update cu query

                                    CoroutineScope(Dispatchers.IO).launch {
                                        var listaNouaQuery = doSongsQuery()
                                        Song.compareAndCompleteTheFirst(
                                            listaNouaQuery,
                                            SongsGlobalVars.allSongs
                                        )

                                        SongsGlobalVars.allSongs.clear()
                                        SongsGlobalVars.allSongs.addAll(listaNouaQuery)

                                        redistributeLists()

                                        // save songs
                                        saveSongLists()
                                        //
                                        withContext(Dispatchers.Main) {
                                            bus.post(Events.GlobalDataWasUpdated())
                                            SongsGlobalVars.refreshBufferIsFree=true
                                        }

                                    }

                                } else {
                                    // Everything is in order

                                    bus.post(Events.GlobalDataWasUpdated())
                                    SongsGlobalVars.refreshBufferIsFree=true

                                }
                            }

                }
            }
        }
    }


    /**
     * Reloads from memory ONLY "SongsGlobalValues.alllist". It does NOT distribute NOR refresh any other playlists/lists
     * */
    suspend fun refreshGlobalSongList(){
        return withContext(Dispatchers.IO) {
            SongsGlobalVars.allSongs.clear()            // am facut load la lista de songs. Fac load si la playlists, si split pe public si hidden songs.
            SongsGlobalVars.allSongs.addAll(Functions.loadFromJson(Application.instance, "GlobalSongs", SongsGlobalVars.allSongs))

            true
        }
    }

    /**
     * Reloads from memory all lists except the pre-existing "SongsGlobalValues.alllist". It does NOT also redistribute from the global list. Call redistributeLists() for that.
     * */
    suspend fun refreshSongLists():Boolean{
        return withContext(Dispatchers.IO) {

            while(!restoreBuffer_free){delay(50)}
            if(restoreBuffer_free) {
                restoreBuffer_free=false
                SongsGlobalVars.playlistsList.clear()
                SongsGlobalVars.playlistsList.addAll(
                    Functions.loadFromJson(
                        Application.instance,
                        "PlaylistsList",
                        ArrayList<Playlist>()
                    )
                )

                SongsGlobalVars.playlistsList.forEach {
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
                    Playlist("Recently Played", null, true)
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
                SongsGlobalVars.publicSongs.songsList?.sortBy { p->p.title }
                SongsGlobalVars.hiddenSongs.songsList?.sortBy { p->p.title }

                restoreBuffer_free=true
            }

            true
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
            SongsGlobalVars.playlistsList.forEach {
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
            SongsGlobalVars.publicSongs.songsList?.sortBy { p->p.title }
            SongsGlobalVars.hiddenSongs.songsList?.sortBy { p->p.title }

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
                    SongsGlobalVars.playlistsList
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


    override fun onPause() {
        super.onPause()
        //Glide.getPhotoCacheDir(this)?.deleteRecursively()


        SongMain_Activity.ActiveTracker.isPaused=true

    }


    suspend fun doSongsQuery(alsoPictures: Boolean = true): ArrayList<Song> =
        withContext(Dispatchers.IO) {
            val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val lista = ArrayList<Song>()


            val queryStartTime: LocalTime = LocalTime.now()


            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

            val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%")

            // Initialize adapter with empty list


            // Phase 1: Stream items as they're found

            val cursor = contentResolver.query(
                FROM, projection, selection, selectionArgs, null
            )

            cursor?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val display_name =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val pieces = display_name.split(".")
                    var title = pieces.concatenateWith(".", listOf(pieces.size-1))

                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val author =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val dateAddedinSeconds =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                    val contentUri = ContentUris.withAppendedId(FROM, id)


                    var thumbnailFile: File = File("")

                    if (alsoPictures) {
                        val thumbnail = try {
                            decodeSampledBitmapFromUri(contentResolver,contentUri,400,400)
                        } catch (ex: Exception) {
                            null
                        }


                        if (thumbnail != null) {
                            Functions.Images.saveToFile(
                                contentUri.lastPathSegment.toString(), thumbnail,
                                SongsGlobalVars.musicDirectory(applicationContext)
                            )
                        }

                        thumbnailFile = File(
                            SongsGlobalVars.musicDirectory(applicationContext),
                            contentUri.lastPathSegment.toString() + ".jpg"
                        )
                    }


                    val song = Song(
                        contentUri.toString(),
                        title,
                        thumbnailFile.toString(),
                        author,
                        duration,
                        dateAddedinSeconds
                    )


                    // Add to both lists
                    lista.add(song)
                    //adaptor.mList.add(song)


                }


                Log.i(
                    "TESTS",
                    "(Main_Activity) Query took ${
                        Duration.between(queryStartTime, LocalTime.now()).toMillis()
                    } miliseconds and had a lsit of size ${lista.size}"
                )


            }

            return@withContext lista
        }



}