package SongsMain.Classes

import DataClasses_Ojects.Logs
import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext

object myMediaPlayer {


    var mediaplayer: MediaPlayer = MediaPlayer()
    private var isInitialized=false
    var currentlyPlayingSong:Song? = null
    val currentPlaylist: Playlist? = null
    var isLoopingInPlaylist:Boolean = true


    fun initializeMediaPlayer(context: Context) {
        if (!isInitialized) {
            mediaplayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Don't prepare here - wait until setDataSource() is called
                isInitialized = true
                Log.i(Logs.MEDIA_SOUND.toString(), "MediaPlayer initialized (no source)")
            }
        }
    }



init {

}



    fun setSong(activity: Activity, song:Song) {
        try {
            if (!mediaplayer.isPlaying){
                mediaplayer.apply {
                    activity.applicationContext.contentResolver.openFileDescriptor(
                        song.songUri.toUri(),
                        "r"
                    ).use { pfd ->


                        setDataSource(pfd?.fileDescriptor)
                        prepareAsync()


                        song.timesListened++
                        song.lastPlayed= LocalDateTime.now()
                        currentlyPlayingSong=song





                    }
                }



            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(), "Cannot set song when mediaplayer is not initialized. FROM SET SONG")
        }
    }

    fun start() {
        mediaplayer.setOnPreparedListener {
            mediaplayer.start()
        }

        if(currentlyPlayingSong!=null)
            mediaplayer.start()
    }

    fun stop() {
        if(currentlyPlayingSong!=null) {
            mediaplayer.stop()
            myMediaPlayer.currentlyPlayingSong=null
            try {
                //mediaplayer.prepare()
            } catch (e: Exception) {
                Log.e(Logs.MEDIA_SOUND.toString(), "Error preparing after stop", e)
            }
        }
    }

    fun toggle(){
        try {
            if (mediaplayer.isPlaying) {
                myMediaPlayer.pause()
            } else {
                myMediaPlayer.start()
            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(),"Mediaplayer has been released or has never been initialized, FROM TOGGLE")
        }
    }


    val isPlaying get() = mediaplayer.isPlaying

    fun reset() {
            mediaplayer.reset()
    }

    fun release() {
        mediaplayer.release()
    }

    fun pause(){
        if(currentlyPlayingSong!=null)
            mediaplayer.pause()
    }


}