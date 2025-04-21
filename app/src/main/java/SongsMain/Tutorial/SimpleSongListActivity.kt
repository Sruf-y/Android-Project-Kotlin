package SongsMain.Tutorial



import DataClasses_Ojects.Logs
import GlobalValues.Alarme.recycleState
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import SongsMain.Classes.SongListAdapter
import SongsMain.Classes.SongsGlobalVars
import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.composepls.R
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.greenrobot.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime
import GlobalValues.Media.internalList
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView

class SimpleSongListActivity : AppCompatActivity() {

    var lastposition=0

    lateinit var nestedscrollview: NestedScrollView

    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter

    lateinit var recyclerLayoutManager: LinearLayoutManager

    var queryFinished = false

    var reloadRequestFull = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_simple_song_list)


        Log.i("TESTS", "SimplesongList created once! +${LocalTime.now()}")

        // setting adaptor
        adaptor = SongListAdapter(
            ArrayList(),
            this,
            { song ->
                if (myMediaPlayer.currentlyPlayingSong != song) {
                    myMediaPlayer.reset()
                    myMediaPlayer.setSong(this, song)
                }

                myMediaPlayer.toggle()
            }, {

                myMediaPlayer.stop()
            }
        )
        // setting linear layoutmanager
        recyclerLayoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        //setting eventbus
        val bus: EventBus? = EventBus.getDefault()

        audioRecycler = this.findViewById(R.id.songView);




        Log.i("TESTS", "SimpleSongList viewcreated once! +${LocalTime.now()}")




        // setting recyclerview
        audioRecycler.setItemViewCacheSize(20) // Cache more views offscreen
        audioRecycler.setHasFixedSize(true) // If items have consistent size

        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = recyclerLayoutManager


        // getting scroll position





        nestedscrollview=this.findViewById(R.id.mynestedScrollview)










        if (savedInstanceState == null) {

            //audioRecycler.recycledViewPool.setMaxRecycledViews(0, 15)

            // adaptor

            internalList.clear()
            internalList.addAll(Functions.loadFromJson(this, "GlobalSongs", internalList))


            // actual loading into recycler


            if (internalList.isEmpty()) {

                lifecycleScope.launch {
                    while (!queryFinished) {
                        delay(50)
                    }
                    // query is finished, i have the list inside the adapter CHECK THE FIRST FUNCTION DOWN!!!!!!!!
                }



                Log.i(Logs.FILE_IO.toString(), "Loading global songs from external query")
                queryAndUpdateSongsRecycler()
            } else {
                Log.i(Logs.FILE_IO.toString(), "Loading global songs from internal")

                adaptor.mList.clear()
                adaptor.mList.addAll(internalList)
                adaptor.notifyItemRangeInserted(0, adaptor.mList.size)


            }


        } else {
            // onReCreate


            adaptor.mList.addAll(internalList)



            adaptor.notifyDataSetChanged()

        }


        val butonplaystop = this.findViewById<Button>(R.id.button7)

        butonplaystop.setOnClickListener {
            lifecycleScope.launch {
                queryAndUpdateSongsRecycler()
            }


        };

        audioRecycler.post {
            audioRecycler.requestFocus()
        }



        var sf: SharedPreferences = this.getSharedPreferences("My SF",Context.MODE_PRIVATE);
        lastposition=sf.getInt("SimpleSongListScrollPosition",0);

        lifecycleScope.launch {
            nestedscrollview.isSmoothScrollingEnabled=true
            audioRecycler.post {

                nestedscrollview.smoothScrollTo(0,lastposition,2000)
            }


            audioRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)

                   nestedscrollview.post {
                       Log.i("TESTS","Recycler adaptor mlist size = "+adaptor.mList.size.toString())
                       Log.i("TESTS","Recycler scrollY = "+audioRecycler.scrollY.toString())

                       lastposition = nestedscrollview.scrollY

                       Log.i("TESTS", lastposition.toString())
                   }

                }
            })
        }






    }


    override fun onPause() {
        super.onPause();
        var editor : SharedPreferences.Editor;
        var sf: SharedPreferences = this.getSharedPreferences("My SF",Context.MODE_PRIVATE);

        editor= sf.edit()

        editor.apply{
            putInt("SimpleSongListScrollPosition",lastposition)
            editor.apply()
        }
    }






    // does the query and then updates the data
    fun queryAndUpdateSongsRecycler(){

        if(!reloadRequestFull) {
            reloadRequestFull = true
            lifecycleScope.launch(Dispatchers.IO) {


                // NEED TO DO THIS TWICH, A 2ND TIME AT THE END AND COMPARE THEM


                val newList = doQuery()





                Functions.saveAsJson(this@SimpleSongListActivity, "GlobalSongs", newList)


                if (adaptor.mList.isEmpty()) {
                    //newly creating list

                    withContext(Dispatchers.Main) {

                        adaptor.mList = newList
                        adaptor.mList.forEachIndexed { index,song->
                            adaptor.notifyItemInserted(index)
                        }


                        return@withContext
                    }
                    Log.i(Logs.MEDIA_SOUND.toString(), "Global songs list creation completed")
                } else {// if adaptor list not empty, re-flush it

                    withContext(Dispatchers.Main) {


                        lifecycleScope.launch {
                            adaptor.mList= withContext(Dispatchers.Main) {


                                adaptor.mList = newList
                                adaptor.notifyDataSetChanged()
                                return@withContext newList
                            }

                        }

                        return@withContext
                        Log.i(Logs.MEDIA_SOUND.toString(), "Global songs appending/removal completed")

//


                    }
                }

                internalList.clear()
                internalList.addAll(adaptor.mList)
                reloadRequestFull=false
            }
        }

    }

    suspend fun doQuery(): ArrayList<Song> =withContext(Dispatchers.IO){
        val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val lista = ArrayList<Song>()

        queryFinished=false
        val queryStartTime: LocalTime=LocalTime.now()


        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST
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
                val contentUri = ContentUris.withAppendedId(FROM, id)


                val thumbnail = try{contentResolver?.loadThumbnail(contentUri, Size(200, 200),null)}catch(ex: Exception){null}


                Functions.Images.saveToFile(contentUri.lastPathSegment.toString(),thumbnail,
                    File(filesDir,"MusicDir"))

                val thumbnailFile = File(File(filesDir,"MusicDir"),contentUri.lastPathSegment.toString()+".jpg")

                val song = Song(
                    contentUri.toString(), title, thumbnailFile,author, duration
                )


                // Add to both lists
                lista.add(song)
                //adaptor.mList.add(song)



            }

            queryFinished=true
            Log.i("TESTS","Query took ${Duration.between(queryStartTime, LocalTime.now()).toMillis()} miliseconds")


        }

        return@withContext lista
    }







}