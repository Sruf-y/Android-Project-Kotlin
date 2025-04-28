package SongsMain.Tabs

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import SongsMain.Classes.SongListAdapter
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Tutorial.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.composepls.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.greenrobot.event.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import java.io.File

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list) {



    lateinit var swipe_to_refresh: SwipeRefreshLayout
    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter

    lateinit var recyclerLayoutManager: LinearLayoutManager

    var recycleState: Parcelable?=null

    val designatedList: ArrayList<Song>
        get() {
            return SongsGlobalVars.publicSongs.songsList!!
        }



    val bus: EventBus = EventBus.getDefault()




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




        adaptor.mList=designatedList
        adaptor.notifyDataSetChanged()






        swipe_to_refresh= requireView().findViewById<SwipeRefreshLayout>(R.id.swiperefresh)

        swipe_to_refresh.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                bus.post(Events.RequestGlobalDataUpdate())
                audioRecycler.visibility=View.GONE

                requireView().findViewById<ProgressBar>(R.id.progbar).visibility=View.VISIBLE
            }
        }





        var sf: SharedPreferences = requireContext().getSharedPreferences("My SF",Context.MODE_PRIVATE);





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
            //adaptor.notifyItemRangeInserted(0,adaptor.mList.size)
            adaptor.notifyDataSetChanged()




        }
        else{
//            val modifications = Functions.differencesBetweenArrays(adaptor.mList,designatedList)
//
//            modifications.forEach {
//                // THIS DEPENDS ON THE MEDIASTORE'S NATURE OF ADDING SONGS IN COADA!!!
//                if(it.typeOfUpdate== TypeOfUpdate.added) {
//                    adaptor.mList.add(it.item)
//                    adaptor.notifyItemInserted(adaptor.mList.size)
//                }
//                else if(it.typeOfUpdate== TypeOfUpdate.removed){
//                    adaptor.mList.remove(it.item)
//                    adaptor.notifyItemRemoved(adaptor.mList.size)
//                }
//                else if(it.typeOfUpdate== TypeOfUpdate.modify){
//                    val position = adaptor.mList.indexOf(it.item)
//                    adaptor.mList[position]=it.item
//                    adaptor.notifyItemChanged(position)
//                }
//            }

            adaptor.mList=designatedList
            adaptor.notifyDataSetChanged()

        }










        audioRecycler.post {



            requireView().findViewById<ProgressBar>(R.id.progbar).visibility=View.GONE

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

            swipe_to_refresh.isRefreshing=false


        }


    }

    fun onEvent(event:Events.SongWasChanged){


        if(event.lastSong!=null && adaptor.mList.contains(event.lastSong)) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.lastSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
        }





        if(event.currentSong!=null && adaptor.mList.contains(event.currentSong)) {
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
        // not working right now




        recycleState = audioRecycler.layoutManager?.onSaveInstanceState()
        //Functions.saveParcelableToFile(Application.instance,"SimpleSongList ScrollPosition",recycleState,File(requireContext().filesDir,"Various Saved Values"))
    }

    override fun onResume() {
        super.onResume()

        //recycleState=Functions.loadParcelableFromFile(Application.instance,"SimpleSongList ScrollPosition", LinearLayoutManager.SavedState.CREATOR ,File(requireContext().filesDir,"Various Saved Values"))
        audioRecycler.post {
            audioRecycler.layoutManager?.onRestoreInstanceState(recycleState)
        }

        audioRecycler.layoutManager?.onRestoreInstanceState(recycleState)

    }











}