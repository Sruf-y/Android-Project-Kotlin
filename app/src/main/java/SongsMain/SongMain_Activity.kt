package SongsMain

import DataClasses_Ojects.Logs
import Functions.AskForPermissionsAtStart
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import android.content.ContentUris
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime


class OpenDrawerEvent()




class SongMain_Activity : AppCompatActivity() {

    val bus:EventBus = EventBus.getDefault();

    lateinit var fragmentContainer: FragmentContainerView
    lateinit var drawer: DrawerLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setContentView(R.layout.activity_songs_main_activity)





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
















        myMediaPlayer.initializeMediaPlayer(this)

        bus.register(this)



    }

    fun onEvent( event: OpenDrawerEvent){
        drawer.open()
    }

    fun onEvent(event:Events.RequestGlobalDataUpdate){

        lifecycleScope.launch {
            SongsGlobalVars.allSongs.clear()
            SongsGlobalVars.allSongs.addAll(doSongsQuery())
            bus.post(Events.GlobalDataWasUpdated())
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
        //myMediaPlayer.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        AskForPermissionsAtStart( this,GlobalValues.System.RequiredPermissions.subList(0,2))


        SongsGlobalVars.allSongs.clear()
        SongsGlobalVars.allSongs.addAll(Functions.loadFromJson(this, "GlobalSongs", SongsGlobalVars.allSongs))

        var songsList: ArrayList<Song> = ArrayList<Song>()
        lifecycleScope.launch {
            // fac un query light la inceputul aplicatiei, verific daca datele stocate sunt la fel ca cele din query. Daca da, continui. Daca nu, fac query heavy
            // Dupa ambele,la final, fac request ca datele din restul listelor si recyclelor sa fie updatate

            songsList=doSongsQuery(false)
            Log.i("TESTS","(Main_Activity) AllSongs list has a size of ${SongsGlobalVars.allSongs.size}")

        }.invokeOnCompletion {

            // get the list of items that are not in concordance with the light query and handle them

            val listInNeedOfUpdates =
                Functions.differencesBetweenArrays(songsList, SongsGlobalVars.allSongs).apply {

                    if (this.isNotEmpty()) {

                        Log.i("TESTS", "(Main_Activity) List in need of udpates of size " + this.size.toString())
                        // exista elemente in neconcordata, fac update cu query

                        lifecycleScope.launch {
                            var listaNouaQuery = doSongsQuery()
                            Song.compareAndCompleteTheFirst(
                                listaNouaQuery,
                                SongsGlobalVars.allSongs
                            )
                            SongsGlobalVars.allSongs.clear()
                            SongsGlobalVars.allSongs.addAll(listaNouaQuery)


                            Functions.saveAsJson(
                                this@SongMain_Activity,
                                "GlobalSongs",
                                SongsGlobalVars.allSongs
                            )
                            bus.post(Events.GlobalDataWasUpdated())
                        }

                    }
                }

        }


    }


    override fun onPause() {
        super.onPause()
        //Glide.getPhotoCacheDir(this)?.deleteRecursively()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Song.quickSaveGlobalList(this@SongMain_Activity)
            }

        }

    }


        suspend fun doSongsQuery(alsoPictures:Boolean=true): ArrayList<Song> =withContext(Dispatchers.IO){
            val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val lista = ArrayList<Song>()


            val queryStartTime: LocalTime=LocalTime.now()


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
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val author =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val dateAddedinSeconds =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                    val contentUri = ContentUris.withAppendedId(FROM, id)


                    var thumbnailFile:File=File("")

                    if(alsoPictures) {
                        val thumbnail = try {
                            contentResolver?.loadThumbnail(
                                contentUri,
                                Size(200, 200),
                                null
                            )
                        } catch (ex: Exception) {
                            null
                        }


                        if (thumbnail != null) {
                            Functions.Images.saveToFile(
                                contentUri.lastPathSegment.toString(), thumbnail,
                                SongsGlobalVars.musicDirectory(applicationContext)
                            )
                        }

                        thumbnailFile = File(SongsGlobalVars.musicDirectory(applicationContext),contentUri.lastPathSegment.toString()+".jpg")
                    }



                    val song = Song(
                        contentUri.toString(), title, thumbnailFile.toString(),author, duration,dateAddedinSeconds
                    )


                    // Add to both lists
                    lista.add(song)
                    //adaptor.mList.add(song)



                }


                Log.i("TESTS","(Main_Activity) Query took ${Duration.between(queryStartTime, LocalTime.now()).toMillis()} miliseconds and had a lsit of size ${lista.size}")


            }

            return@withContext lista
        }

}