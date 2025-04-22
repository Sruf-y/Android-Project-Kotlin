package SongsMain.Classes

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events.SongWasStopped
import SongsMain.Classes.Song.Companion.from
import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import de.greenrobot.event.EventBus
import java.time.LocalDateTime

object myMediaPlayer {

    val bus: EventBus = EventBus.getDefault()
    var mediaplayer: MediaPlayer = MediaPlayer()
    private var isInitialized=false
    var currentlyPlayingSong:Song? = null
    val currentPlaylist: Playlist? = null
    var isLoopingInPlaylist:Boolean = true


    fun initializeMediaPlayer(context: Context) {
        //mediaplayer.reset()
        if (!isInitialized) {
            Log.i("TESTS","Media initialize requested")
            mediaplayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Don't prepare here - wait until setDataSource() is called
                isInitialized = true


                Log.i(Logs.MEDIA_SOUND.toString(), "MediaPlayer initialized")
            }
        }
    }






    fun setSong(activity: Activity, song:Song) {


        try {
            if (!mediaplayer.isPlaying){
                Log.i("TESTS","Set song requested")
                mediaplayer.apply {
                    activity.applicationContext.contentResolver.openFileDescriptor(
                        song.from(SongsGlobalVars.allSongs)!!.songUri.toUri(),
                        "r"
                    ).use { pfd ->

                        setDataSource(pfd?.fileDescriptor)
                        prepareAsync()


                        song.from(SongsGlobalVars.allSongs)?.timesListened++
                        song.from(SongsGlobalVars.allSongs)?.lastPlayed= LocalDateTime.now().toString()

                        bus.post(Events.SongWasChanged(currentlyPlayingSong,song.from(SongsGlobalVars.allSongs)))

                        currentlyPlayingSong=song.from(SongsGlobalVars.allSongs)


                        Log.i(Logs.MEDIA_SOUND.toString(), "Mediaplayer song set to ${currentlyPlayingSong?.title}")



                    }
                }



            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(), "Cannot set song when mediaplayer is not initialized. FROM SET SONG")
        }
    }

    fun start() {

        // also set the prepared listener
        mediaplayer.setOnPreparedListener {
            mediaplayer.start()
            bus.post(Events.SongWasStarted())
        }


        if(currentlyPlayingSong!=null && !isPlaying) {
            Log.i("TESTS","Start song requested")
            mediaplayer.start()
            bus.post(Events.SongWasStarted())
        }



    }

    fun stop() {
        if(currentlyPlayingSong!=null) {
            Log.i("TESTS","Stop song requested")
            mediaplayer.stop()
            myMediaPlayer.currentlyPlayingSong=null
            bus.post(SongWasStopped())
        }
    }

    fun toggle(){
        try {
            if (myMediaPlayer.currentlyPlayingSong != null) {
                Log.i("TESTS", "Toggle song requested")
                if (mediaplayer.isPlaying) {
                    myMediaPlayer.pause()

                } else {
                    myMediaPlayer.start()

                }
            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(),"Mediaplayer has been released or has never been initialized, FROM TOGGLE")
        }
    }


    val isPlaying get() = mediaplayer.isPlaying

    fun getCurrentPosition(): Int {

        return mediaplayer.currentPosition

    }


    fun reset() {
        Log.i("TESTS","Reset song requested")
        mediaplayer.reset()
        bus.post(Events.SongWasReset())
    }

    fun release() {
        mediaplayer.release()
    }

    fun pause(){
        if(currentlyPlayingSong!=null) {
            mediaplayer.pause()
            bus.post(Events.SongWasPaused())

        }
    }


}