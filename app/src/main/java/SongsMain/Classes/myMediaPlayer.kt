package SongsMain.Classes

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events.SongWasStopped
import SongsMain.Classes.Song.Companion.from
import SongsMain.Tutorial.Application
import SongsMain.Tutorial.MusicPlayerService
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import de.greenrobot.event.EventBus
import java.time.LocalDateTime

object myMediaPlayer {

    val bus: EventBus = EventBus.getDefault()
    var mediaplayer: MediaPlayer = MediaPlayer()
    private var isInitialized=false
    var currentlyPlayingSong:Song? = null
    var currentPlaylist: Playlist? = null
    var isLoopingInPlaylist:Boolean = false // in the future if i add a loop button



    fun initializeMediaPlayer() {
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



                Log.i(Logs.MEDIA_SOUND.toString(), "MediaPlayer initialized")
            }
            isInitialized = true
        }
    }


    fun seekTo(under100:Long){
        if(myMediaPlayer.currentlyPlayingSong!=null){
            try{
                mediaplayer.seekTo(((under100/100)*currentlyPlayingSong!!.duration).toInt())
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    fun openPlaylist(playlist: Playlist?){
        if(isInitialized){

            if(playlist!=null){

                if(playlist.songsList!=null)
                {
                    if(playlist.songsList!!.isNotEmpty()){
                        this.currentPlaylist=playlist

                        // tells it to play the next in playlist IF the playlist was initialized. This means it will play until it stops finding a "next" song.
                        mediaplayer.setOnCompletionListener {
                            myMediaPlayer.playNextInPlaylist()
                        }
                    }
                }
            }

        }
    }

    fun playNextInPlaylist(){
        if(isInitialized&& currentlyPlayingSong!=null){
            if(this.currentPlaylist!=null){
                if(currentPlaylist!!.hasNextAfter(myMediaPlayer.currentlyPlayingSong!!))
                {
                    val currentIndex:Int = currentPlaylist!!.songsList!!.indexOf(currentlyPlayingSong)

                    this.setSong(currentPlaylist!!.songsList!![currentIndex+1])
                }
            }
        }
    }

    fun playPreviousInPlaylist(){
        if(isInitialized && currentlyPlayingSong!=null) {
            if (this.currentPlaylist != null) {
                if(currentPlaylist!!.hasPreviousBefore(currentlyPlayingSong!!))
                {
                    val currentIndex:Int = currentPlaylist!!.songsList!!.indexOf(currentlyPlayingSong)

                    this.setSong(currentPlaylist!!.songsList!![currentIndex-1])
                }
            }
        }
    }

    fun setVolume(leftVolume:Float,rightVolume:Float){
        if(isInitialized){
            mediaplayer.setVolume(leftVolume,rightVolume)
        }
    }



    fun getAudioSessionID(): Int {
        return mediaplayer.audioSessionId
    }


    fun setSong(song:Song) {


        initializeMediaPlayer()


        try {
            if (!mediaplayer.isPlaying){
                Log.i("TESTS","Set song requested")
                mediaplayer.apply {
                    Application.instance.applicationContext.contentResolver.openFileDescriptor(
                        song.from(SongsGlobalVars.allSongs)!!.songUri.toUri(),
                        "r"
                    ).use { pfd ->

                        setDataSource(pfd?.fileDescriptor)
                        prepareAsync()


                        mediaplayer.setOnPreparedListener {





                            val lastSong = currentlyPlayingSong
                            currentlyPlayingSong=song.from(SongsGlobalVars.allSongs)


                            bus.post(Events.SongWasChanged(lastSong,currentlyPlayingSong))
                            _Playing=true


                            // update the stats too
                            song.from(SongsGlobalVars.allSongs)?.timesListened++
                            song.from(SongsGlobalVars.allSongs)?.lastPlayed= LocalDateTime.now().toString()

                            SongsGlobalVars.playingQueue.apply {
                                if (this.contains(song.from(SongsGlobalVars.allSongs))) {
                                    this.remove(song.from(SongsGlobalVars.allSongs))
                                }
                                this.add(song.from(SongsGlobalVars.allSongs)!!)
                            }

                            myMediaPlayer.start()
                        }


                        Log.i(Logs.MEDIA_SOUND.toString(), "Mediaplayer song set to ${currentlyPlayingSong?.title}")



                    }
                }



            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(), "Cannot set song when mediaplayer is not initialized. FROM SET SONG")
        }
    }

    fun start() {

        if(currentlyPlayingSong!=null && !isPlaying) {
            Log.i("TESTS","Start song requested")
            mediaplayer.start()
            _Playing=true
            bus.post(Events.SongWasStarted())
        }

    }

    fun stop() {
        if(currentlyPlayingSong!=null) {
            Log.i("TESTS","Stop song requested")
            mediaplayer.stop()
            myMediaPlayer.currentlyPlayingSong=null
            _Playing=false
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

     var _Playing = false
    var isPlaying
        get() = try{mediaplayer.isPlaying}catch(ex:Exception){
        myMediaPlayer.initializeMediaPlayer()
            _Playing
    }
        set(value) {_Playing=value}

    fun getCurrentPosition(): Int {

        return mediaplayer.currentPosition

    }


    fun reset() {

        if(isInitialized) {

            if (currentlyPlayingSong != null) {

                mediaplayer.reset()
                _Playing=false
                bus.post(Events.SongWasReset())
            }
        }
    }

    fun release() {
        mediaplayer.release()
        _Playing=false
    }

    fun pause(){
        if(currentlyPlayingSong!=null) {
            mediaplayer.pause()
            _Playing=false
            bus.post(Events.SongWasPaused())

        }
    }




}