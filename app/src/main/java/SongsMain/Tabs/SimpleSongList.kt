package SongsMain.Tabs

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.greenrobot.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalTime
import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView
import androidx.compose.runtime.DisposableEffect
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import kotlinx.coroutines.CoroutineScope

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list) {

    var lastposition=0
    lateinit var nestedscrollview: NestedScrollView

    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter

    lateinit var recyclerLayoutManager: LinearLayoutManager

    val designatedList: ArrayList<Song>
        get() {
            return SongsGlobalVars.publicSongs.songsList!!
        }



    val bus: EventBus = EventBus.getDefault()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)








    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        audioRecycler = requireView().findViewById(R.id.songView);
        nestedscrollview=requireView().findViewById(R.id.mynestedScrollview)

        Log.i("TESTS", "SimplesongList created once! +${LocalTime.now()}")

        adaptor = SongListAdapter(
            ArrayList(),
            requireContext(),
            { song ->

                    myMediaPlayer.initializeMediaPlayer()
                    if(myMediaPlayer.iPrepared_)
                        myMediaPlayer.reset()
                    myMediaPlayer.setSong( song)

                myMediaPlayer.start()
            }, {song->

                myMediaPlayer.stop()
            }
        )

        recyclerLayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        Log.i("TESTS", "SimpleSongList viewcreated once! +${LocalTime.now()}")





        audioRecycler.setItemViewCacheSize(20) // Cache more views offscreen
        audioRecycler.setHasFixedSize(true) // If items have consistent size

        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = recyclerLayoutManager



        Log.i(Logs.FILE_IO.toString(), "Handled recreation of songlist fragment from internal")

        val butonplaystop = requireView().findViewById<Button>(R.id.button7)









        // LOADING SCREEN HERE ////
        requireView().findViewById<ConstraintLayout>(R.id.progbar).visibility=View.VISIBLE
        butonplaystop.visibility= View.INVISIBLE
        audioRecycler.visibility=View.INVISIBLE
        adaptor.mList= designatedList
        adaptor.notifyItemRangeInserted(0,adaptor.mList.size)

            // after everything is good and ready
        audioRecycler.post {
            if(adaptor.mList.isNotEmpty()) {
                requireView().findViewById<ConstraintLayout>(R.id.progbar).visibility =
                    View.INVISIBLE
                butonplaystop.visibility = View.VISIBLE
                audioRecycler.visibility = View.VISIBLE
            }
        }


        /////////////////////////

















        butonplaystop.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                bus.post(Events.RequestGlobalDataUpdate())
                butonplaystop.visibility= View.INVISIBLE
                audioRecycler.visibility=View.INVISIBLE

                requireView().findViewById<ConstraintLayout>(R.id.progbar).visibility=View.VISIBLE
            }

        }





        var sf: SharedPreferences = requireContext().getSharedPreferences("My SF",Context.MODE_PRIVATE);
        lastposition=sf.getInt("SimpleSongListScrollPosition",0);

        CoroutineScope(Dispatchers.Main).launch {
            nestedscrollview.isSmoothScrollingEnabled=true

            //only scroll on opening the app

            audioRecycler.post {

                nestedscrollview.smoothScrollTo(0, lastposition, 500)
            }




            nestedscrollview.setOnScrollChangeListener { view: NestedScrollView, _, scrollY, _, _ ->
                nestedscrollview.post {

                    lastposition = nestedscrollview.scrollY

                }
            }


        }




        bus.register(this)
        audioRecycler.post {
            onEvent(Events.SongWasChanged(null, myMediaPlayer.currentlyPlayingSong))
        }

    }

    fun onEvent(event:Events.GlobalDataWasUpdated){
        Log.i("TESTS","Global data was updated via event and this is from simplesonglist")


        val mlistSizeWas = adaptor.mList.size
            adaptor.mList= designatedList
        if(mlistSizeWas==0){
            adaptor.notifyItemRangeInserted(0,adaptor.mList.size)
        }
        else{
            adaptor.notifyDataSetChanged()
        }










        audioRecycler.post {

            val butonplaystop = requireView().findViewById<Button>(R.id.button7)
            butonplaystop.visibility=View.VISIBLE
            requireView().findViewById<ConstraintLayout>(R.id.progbar).visibility=View.INVISIBLE

            audioRecycler.visibility=View.VISIBLE


            //don't forget about visually setting the currently playing song
            if(myMediaPlayer.currentlyPlayingSong!=null && adaptor.mList.contains(myMediaPlayer.currentlyPlayingSong)) {
                (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(myMediaPlayer.currentlyPlayingSong))
                    ?.findViewById<TextView>(R.id.title))?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.amber
                        )
                    )
            }


        }


    }

    fun onEvent(event:Events.SongWasChanged){


        if(event.lastSong!=null) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.lastSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
        }





        if(event.currentSong!=null) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.currentSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.amber
                )
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        bus.unregister(this)
    }


    override fun onPause() {
        super.onPause()
        var editor : SharedPreferences.Editor;
        var sf: SharedPreferences = requireContext().getSharedPreferences("My SF",Context.MODE_PRIVATE);

        editor= sf.edit()

        editor.apply{
            putInt("SimpleSongListScrollPosition",lastposition)
            editor.apply()
        }
    }











}