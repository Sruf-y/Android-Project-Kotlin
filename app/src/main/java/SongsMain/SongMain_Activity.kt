package SongsMain

import DataClasses_Ojects.Logs
import Functions.AskForPermissionsAtStart
import Functions.ViewAttributes
import Functions.concatenateWith
import SongsMain.Classes.Events
import SongsMain.Classes.MyMediaController
import SongsMain.Classes.Playlist
import SongsMain.Classes.Song
import SongsMain.Classes.Song.Companion.takeYourPartFromGlobal
import SongsMain.Classes.myExoPlayer
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Variables.Music_App_Settings
import SongsMain.Tutorial.Application
import SongsMain.Tutorial.MusicPlayerService
import SongsMain.Variables.MusicAppSettings
import SongsMain.Variables.SongsGlobalVars.SongsStorageOperations.redistributeLists
import SongsMain.Variables.SongsGlobalVars.SongsStorageOperations.refreshGlobalSongList
import SongsMain.Variables.SongsGlobalVars.SongsStorageOperations.refreshSongLists
import SongsMain.Variables.SongsGlobalVars.SongsStorageOperations.saveSongLists
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.example.composepls.R
import com.google.android.material.navigation.NavigationView
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime


class OpenDrawerEvent()




class SongMain_Activity : AppCompatActivity(),Player.Listener{

    private val controllerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int){

        }
    }

    val bus:EventBus = EventBus.getDefault();



    lateinit var fragmentContainer: FragmentContainerView
    lateinit var drawer: DrawerLayout
    lateinit var navView: NavigationView

     companion object ActiveTracker {
         lateinit var serviceIntent:Intent

        var isRunningAnywhere: Boolean = true

    }

    var mediaController: MediaController? = null


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_songs_main_activity)

        AskForPermissionsAtStart( this,GlobalValues.System.RequiredPermissions.subList(0,4))

        SongMain_Activity.ActiveTracker.isRunningAnywhere=true





        WindowCompat.setDecorFitsSystemWindows(window, false)



        fragmentContainer=findViewById(R.id.fragmentContainerView)


        if (savedInstanceState == null) {
            Log.i("TESTS","(Main_Activity) Activity set the initial fragment! +${LocalTime.now()}")

            makeCurrentFragment(fragmentContainer, SongsMain.bottomSheetFragment())

        }



        // edge-to-edge //////////////////////////

        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))
        drawer = findViewById(R.id.drawerLayout)

        enableEdgeToEdge(myStatusBarStyle, myNavigationBarStyle)

        navView = requireViewById(R.id.navView)


        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
                        makeCurrentFragment(fragmentContainer,Music_App_Settings(),addtoBackStack = true)

                    }
                }
                drawer.close()

                return true
            }
        })





        // should be getting initialized in the service launcher
        //myMediaPlayer.initializeMediaPlayer(this)

        bus.register(this)





        myExoPlayer.initializePlayer(this)




        MusicAppSettings.restoreSettings()

        if(savedInstanceState==null){
            // restore app settings

            onEvent(Events.SettingsWereChanged())

            doStartDataLoad()
        }






        MyMediaController.initialize(this)

//        val sessionToken = SessionToken(this, ComponentName(this,
//            Media3Service::class.java))
//
//
//
//        val controllerFuture = MediaController.Builder(this,sessionToken).buildAsync()
//        controllerFuture.addListener({
//            if (controllerFuture.isDone){
//                mediaController=controllerFuture.get()
//            }
//        }, MoreExecutors.directExecutor()
//        )
//


        MyMediaController.addListener(this)
        //startMusicService()
    }





     fun updatePlaybackState() {
        val isPlaying = mediaController?.isPlaying ?: false
//        binding.playPauseButton.setImageResource(
//            if (isPlaying) R.drawable.pause_button_music_player else R.drawable.play_button_music_player )

         // same as underneath
    }

     fun updateMetadata() {
        val song = mediaController?.currentMediaItem?.localConfiguration?.tag as? Song
        song?.let {
            // here i can get the song's info in case i need it for some ui or something
        }
    }

    // UI Control Examples
    fun onPlayPauseClick() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun onSkipNextClick() {
        mediaController?.seekToNext()
    }







    fun onEvent(event:Events.MakeCurrentMainFragment){
        makeCurrentFragment(fragmentContainer,event.fragment, addtoBackStack = true)
    }

    fun onEvent(event: Events.SettingsWereChanged){
        MusicAppSettings.applySettings(null,{
            ViewAttributes(navView).Background().Set(this,MusicAppSettings.theme)

            when(MusicAppSettings.orientation){
                0->{requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT }
                1->{requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED}
                2->{requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE}
            }
        })
    }




    fun onEvent(event:Events.SearchButtonPressed){
        val intent = Intent(this, search::class.java)

        startActivity(intent)
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



    fun onEvent(event:Events.RequestGlobalDataUpdate){

        doStartDataLoad()

    }

    fun onEvent( event: Events.SongWasStarted){


        startMusicService()
    }


    fun startMusicService(){
        if(!MusicPlayerService.isServiceRunning())
        {


            serviceIntent = Intent(this, MusicPlayerService::class.java).apply {
                // Add this action to distinguish from normal starts
                action = "ACTION_START_PLAYBACK"
            }


            Log.d("ServiceLifecycle", "onStartCommand with action: ${intent?.action}")

            startForegroundService(serviceIntent)

            bus.post(Events.SongWasChanged(null,null))
        }
    }






//    private fun replaceCurrentFragment(activity: FragmentActivity, container: FragmentContainerView, fragmentClass: Class<out Fragment>, args:Bundle=Bundle()) {
//        activity.supportFragmentManager.commit {
//            setReorderingAllowed(true)
//            replace(container.id, fragmentClass, args, fragmentClass.name)
//        }
//    }

    private fun makeCurrentFragment(container: FragmentContainerView, fragment: Fragment,addtoBackStack:Boolean=false) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        if(addtoBackStack)
            transaction.addToBackStack(null)

        // Remove any existing fragment in the container
        val currentFragment = supportFragmentManager.findFragmentById(container.id)
        if (currentFragment != null) {
            transaction.remove(currentFragment)
        }

        // Add the new fragment with arguments
        transaction.add(container.id, fragment, fragment::class.java.name)
        transaction.commit()
    }

    override fun onDestroy() {
        Log.i(Logs.LIFECYCLE.toString(),"(Main_Activity) MusicPlayer activity destroyed")


        bus.unregister(this)


        MyMediaController.removeListener(this)
        MyMediaController.release()
        mediaController?.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()


    }


    /**
     * Application start loading of data and distribution.
     * */
    @kotlin.OptIn(DelicateCoroutinesApi::class)
    fun doStartDataLoad(){
        if(SongsGlobalVars.globalDataLoadBuffer_Free) {
            SongsGlobalVars.globalDataLoadBuffer_Free=false
            GlobalScope.launch {
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
                                            SongsGlobalVars.globalDataLoadBuffer_Free=true
                                        }

                                    }

                                } else {
                                    // Everything is in order

                                    bus.post(Events.GlobalDataWasUpdated())
                                    SongsGlobalVars.globalDataLoadBuffer_Free=true

                                }
                            }

                }
            }
        }
    }




    override fun onPause() {
        super.onPause()
        //Glide.getPhotoCacheDir(this)?.deleteRecursively()

        CoroutineScope(Dispatchers.IO).launch {

            val savedSuccesfully = saveSongLists()




        }


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
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

            val selection =null //"${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs =null //arrayOf("%")

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
                    val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val contentUri = ContentUris.withAppendedId(FROM, id)


                    var thumbnailFile: File = File("")

                    if (alsoPictures) {
                        val thumbnail = try {
                            //decodeSampledBitmapFromUri(contentResolver,contentUri,400,400)
                            contentResolver.loadThumbnail(contentUri, android.util.Size(500,500),null)
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
                        id= id,
                        songUri = contentUri.toString(),
                        title = title,
                        thumbnail = thumbnailFile.toString(),
                        author = author,
                        duration = duration,
                        dateAdded = dateAddedinSeconds,
                        albumName = albumName
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