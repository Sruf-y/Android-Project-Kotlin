package SongsMain.Tabs

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import SongsMain.Classes.SongListAdapter
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Classes.TypeOfUpdate
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
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.compose.runtime.DisposableEffect
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list) {

    var lastposition=0


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


        Log.i("TESTS", "SimplesongList created once! +${LocalTime.now()}")

        adaptor = SongListAdapter(
            ArrayList(),
            requireContext(),
            { song ->


                myMediaPlayer.initializeMediaPlayer()


                if (myMediaPlayer.iPrepared_)
                    myMediaPlayer.reset()
                myMediaPlayer.setSong(song)
                myMediaPlayer.openPlaylist(SongsGlobalVars.publicSongs)
                myMediaPlayer.start()
            }, {song->


            }
        )

        recyclerLayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        Log.i("TESTS", "SimpleSongList viewcreated once! +${LocalTime.now()}")





        //audioRecycler.setItemViewCacheSize(20) // Cache more views offscreen
        audioRecycler.setHasFixedSize(true) // If items have consistent size
        audioRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade_in)
        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = recyclerLayoutManager



        Log.i(Logs.FILE_IO.toString(), "Handled recreation of songlist fragment from internal")

        val butonplaystop = requireView().findViewById<Button>(R.id.button7)














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


            //only scroll on opening the app

            audioRecycler.post {

                audioRecycler.smoothScrollToPosition( lastposition)
            }




            audioRecycler.setOnScrollChangeListener { recycler, _, scrollY, _, _ ->
                audioRecycler.post {

                    lastposition = audioRecycler.scrollY

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

        // this is where

        val mlistSizeWas = adaptor.mList.size
            //adaptor.mList= designatedList




        if(mlistSizeWas==0){
            adaptor.mList.clear()
            adaptor.mList.addAll(designatedList)
            adaptor.notifyItemRangeInserted(0,adaptor.mList.size)


            audioRecycler.post {

                audioRecycler.smoothScrollToPosition( lastposition)
            }
//            var speed:Long = 100
//            val acceleration =100
//
//            CoroutineScope(Dispatchers.Default).launch {
//                designatedList.forEachIndexed { index, song ->
//                    audioRecycler.post {
//                        adaptor.mList.add(song)
//                        adaptor.notifyItemInserted(index)
//                    }
//                    speed=if((speed-acceleration).toLong()>=0){(speed-acceleration).toLong()}else{0}
//                    delay(speed)
//
//                }
//            }
        }
        else{
            val modifications = Functions.differencesBetweenArrays(adaptor.mList,designatedList)

            modifications.forEach {
                // THIS DEPENDS ON THE MEDIASTORE'S NATURE OF ADDING SONGS IN COADA!!!
                if(it.typeOfUpdate== TypeOfUpdate.added) {
                    adaptor.mList.add(it.item)
                    adaptor.notifyItemInserted(adaptor.mList.size)
                }
                else if(it.typeOfUpdate== TypeOfUpdate.removed){
                    adaptor.mList.remove(it.item)
                    adaptor.notifyItemRemoved(adaptor.mList.size)
                }
                else if(it.typeOfUpdate== TypeOfUpdate.modify){
                    val position = adaptor.mList.indexOf(it.item)
                    adaptor.mList[position]=it.item
                    adaptor.notifyItemChanged(position)
                }
            }

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