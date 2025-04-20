package SongsMain.Tabs

import DataClasses_Ojects.Logs
import GlobalValues.Alarme.recycleState
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import SongsMain.Classes.SongListAdapter
import SongsMain.Classes.SongsGlobalVars
import android.content.ContentUris
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.composepls.R
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.toCoilUri
import de.greenrobot.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list){

    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter

    var appFreshOpen=true

    var queryFinished=false

    var reloadRequestFull=false

    var internalList: ArrayList<Song> = ArrayList<Song>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bus: EventBus? = EventBus.getDefault()


        audioRecycler = requireView().findViewById(R.id.songView);
        audioRecycler.setItemViewCacheSize(20) // Cache more views offscreen
        audioRecycler.setHasFixedSize(true) // If items have consistent size
        //audioRecycler.recycledViewPool.setMaxRecycledViews(0, 15)

        // adaptor
        adaptor = SongListAdapter(
            audioRecycler,
            ArrayList(),
            requireContext(),
            {song->
                if(myMediaPlayer.currentlyPlayingSong!=song) {
                    myMediaPlayer.reset()
                    myMediaPlayer.setSong(requireActivity(), song)
                }

                myMediaPlayer.toggle()
            },{

                myMediaPlayer.stop()
            }
        )
        audioRecycler.layoutManager= LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        audioRecycler.adapter = adaptor







        internalList = Functions.loadFromJson(requireContext(), "GlobalSongs", internalList)


        // actual loading into recycler

        if (appFreshOpen) { //only once per app open
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
                adaptor.notifyItemRangeInserted(0,adaptor.mList.size)


            }
        }
        else{
            // onReCreate
            adaptor.mList.addAll(internalList)
            adaptor.notifyItemRangeInserted(0,adaptor.mList.size)

        }





        val butonplaystop = requireView().findViewById<Button>(R.id.button7)

        butonplaystop.setOnClickListener {
            lifecycleScope.launch {
                queryAndUpdateSongsRecycler()
            }


        }






    }








    // does the query and then updates the data
    fun queryAndUpdateSongsRecycler(){

        if(!reloadRequestFull) {
            reloadRequestFull = true
            lifecycleScope.launch(Dispatchers.IO) {


                // NEED TO DO THIS TWICH, A 2ND TIME AT THE END AND COMPARE THEM


                val newList = doQuery()





                Functions.saveAsJson(requireContext(), "GlobalSongs", newList)


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
                } else {
                    //if (newList.size != adaptor.mList.size) {
                        //adding/removing to list
                        withContext(Dispatchers.Main) {


                            lifecycleScope.launch {
                                adaptor.mList= withContext(Dispatchers.Main) {


                                    adaptor.mList = newList
                                    adaptor.notifyDataSetChanged()
                                    return@withContext newList
                                }

                            }


                            return@withContext
                        //}
                        Log.i(Logs.MEDIA_SOUND.toString(), "Global songs appending/removal completed")

//                    } else {
//                        //updating list
//                        val list_of_items_to_update =
//                            Functions.arrayListNeedingUpdate(adaptor.mList, newList)
//
//                        list_of_items_to_update.forEach {
//                            withContext(Dispatchers.Main) {
//                                adaptor.notifyItemChanged(adaptor.mList.indexOf(it))
//                                return@withContext
//                            }
//                        }
//                        Log.i(Logs.MEDIA_SOUND.toString(), "Global songs update completed")


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

                val cursor = requireActivity().contentResolver.query(
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


                        val thumbnail = try{context?.contentResolver?.loadThumbnail(contentUri, Size(200, 200),null)}catch(ex: Exception){null}


                        Functions.Images.saveToFile(contentUri.lastPathSegment.toString(),thumbnail,
                            SongsGlobalVars.musicDirectory(requireActivity()))

                        val thumbnailFile = File(SongsGlobalVars.musicDirectory(requireActivity()),contentUri.lastPathSegment.toString()+".jpg")

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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recycleState = audioRecycler.layoutManager?.onSaveInstanceState() ?: return
        outState.putParcelable("recycler_state", recycleState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            recycleState = it.getParcelable("recycler_state") ?: return@let
            audioRecycler.layoutManager?.onRestoreInstanceState(recycleState)
        }

    }



    override fun onPause() {
        super.onPause()


    }

    override fun onResume() {
        super.onResume()


    }




}