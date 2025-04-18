package SongsMain.Classes

import DataClasses_Ojects.Logs
import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlin.coroutines.coroutineContext

object myMediaPlayer {


    var mediaplayer: MediaPlayer = MediaPlayer()
    private var isInitialized=false


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
    fun setSong(context: Context, uri: Uri) {
        mediaplayer.apply {
            reset() // Clear previous source
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                setDataSource(pfd.fileDescriptor)
                prepareAsync() // ← Prepare AFTER setting source
            }
        }
    }


    fun setSong(activity: Activity, songURI: String) {
        try {
            if (!mediaplayer.isPlaying){
                mediaplayer.apply {
                    activity.applicationContext.contentResolver.openFileDescriptor(
                        songURI.toUri(),
                        "r"
                    ).use { pfd ->


                        setDataSource(pfd?.fileDescriptor)
                        prepareAsync()
                    }
                }



            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(), "Cannot set song when mediaplayer is not initialized. FROM SET SONG")
        }
    }

    fun start() {
        mediaplayer.setOnPreparedListener {
            //launch the notification maybe?
        }

        mediaplayer.start()
    }

    fun stop() {
        mediaplayer.stop()

        try {
            mediaplayer.prepare()
        } catch (e: Exception) {
            Log.e(Logs.MEDIA_SOUND.toString(), "Error preparing after stop", e)
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
        mediaplayer.pause()
    }


}