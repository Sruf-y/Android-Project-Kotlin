package SongsMain.Tabs

import DataClasses_Ojects.Logs
import GlobalValues.Alarme.recycleState
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import SongsMain.Classes.SongListAdapter
import StorageTest.Classes.Tip_For_adaptor
import android.content.ContentUris
import android.database.Cursor
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import de.greenrobot.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list), SongListAdapter.onClickListener,
    SongListAdapter.onLongPressListener {

    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter<Song>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bus: EventBus? = EventBus.getDefault()


        audioRecycler = requireView().findViewById(R.id.songView);
        audioRecycler.setItemViewCacheSize(20) // Cache more views offscreen
        // audioRecycler.setHasFixedSize(true) // If items have consistent size




















        //Log.i("TESTS","Nr melodii(SimpleSongList): "+lista.size.toString())


        doQuery()




        val butonplaystop = requireView().findViewById<Button>(R.id.button7)




















    }

    fun doQuery(): ArrayList<Song> {
        val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val lista = ArrayList<Song>()

        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DURATION,
            MediaStore.Downloads.ARTIST
        )

        val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%")

        // Initialize adapter with empty list
        adaptor = SongListAdapter(
            ArrayList(),
            Tip_For_adaptor.song,
            requireContext(),
            this@SimpleSongList,
            this@SimpleSongList
        )
        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)

        lifecycleScope.launch {
            // Phase 1: Stream items as they're found
            withContext(Dispatchers.IO) {
                val cursor = requireActivity().contentResolver.query(
                    FROM, projection, selection, selectionArgs, null
                )

                cursor?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                        val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DURATION))
                        val author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.ARTIST))
                        val contentUri = ContentUris.withAppendedId(FROM, id)



                        val thumbnail =
                            try {
                                context?.contentResolver?.loadThumbnail(
                                    contentUri,
                                    Size(200, 200), null
                                )
                            }catch (ex: Exception){
                                null
                            }


                        val song = Song(contentUri.toString(), thumbnail, title, author, duration)

                        // Add to both lists and update UI
                        withContext(Dispatchers.Main) {
                            lista.add(song)
                            adaptor.mList.add(song)
                            adaptor.notifyItemInserted(adaptor.mList.size - 1)
                        }
                    }
                }
            }




        }

        return lista
    }








    override fun onPause() {
        super.onPause()
        recycleState = audioRecycler.layoutManager?.onSaveInstanceState()

    }

    override fun onResume() {
        super.onResume()


    }

    override fun setOnCardClickListener(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {




        if(myMediaPlayer.currentlyPlayingSong!=adaptor.mList[position]) {
            myMediaPlayer.reset()
            myMediaPlayer.setSong(requireActivity(), adaptor.mList[position])
        }else{
            myMediaPlayer.toggle()
        }






    }

    override fun setOnCardLongPressListener(
        position: Int,
        itemViewHolder: RecyclerView.ViewHolder
    ) {
        myMediaPlayer.stop()
    }
}