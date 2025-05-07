package SongsMain.Classes

import DataClasses_Ojects.Logs
import SongsMain.Classes.Events.SongWasStopped
import SongsMain.Classes.Song.Companion.from
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.core.net.toUri
import androidx.lifecycle.AtomicReference
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import de.greenrobot.event.EventBus
import okio.FileNotFoundException
import java.io.File
import java.time.LocalDateTime


 object myMediaPlayer {

    val bus: EventBus = EventBus.getDefault()
    var mediaplayer: MediaPlayer = MediaPlayer()
    private var isInitialized=false
    val isInitialized_: Boolean
        get() {return isInitialized}

    private var isPrepared=false
    val iPrepared_: Boolean
        get() {return isPrepared}


    var currentlyPlayingSong:Song? = null
    var currentPlaylist: AtomicReference<Playlist>?=null
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

    fun rewind(){
        if(myMediaPlayer.currentlyPlayingSong!=null){
            myMediaPlayer.reset()
            myMediaPlayer.setSong(currentlyPlayingSong!!)
        }
    }

    fun seekTo(pos:Long,rawPosition:Boolean=true){
        if(myMediaPlayer.currentlyPlayingSong!=null){
            try{
                if(rawPosition){
                    mediaplayer.seekTo(pos.toInt())
                }else {
                    mediaplayer.seekTo(((pos / 100) * currentlyPlayingSong!!.duration).toInt().coerceIn(0,currentlyPlayingSong!!.duration.toInt()))
                }

                mediaplayer.setOnSeekCompleteListener {
                    bus.post(Events.SongWas_UsedSeek())
                }
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    fun openPlaylist(playlist: AtomicReference<Playlist>){
        if(isInitialized){
            if(playlist!=null){

                if(playlist.get().songsList!=null)
                {

                    if(playlist.get().songsList!!.isNotEmpty()){
                        myMediaPlayer.currentPlaylist=playlist
                        Log.i("TESTS","(myMediaPlayer) Playlist was open succesfully")
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
        if(isInitialized){
            if(this.currentPlaylist!=null){

                if(currentPlaylist!!.get().hasNextAfter(myMediaPlayer.currentlyPlayingSong!!))
                {
                    val currentIndex:Int = currentPlaylist!!.get().songsList!!.indexOf(currentlyPlayingSong)


                    myMediaPlayer.reset()
                    myMediaPlayer.setSong(currentPlaylist?.get()?.songsList!![currentIndex+1].from(
                        SongsGlobalVars.allSongs)!!)
                }
            }
        }
    }

    fun playPreviousInPlaylist(){
        if(isInitialized ) {
            if (this.currentPlaylist != null) {
                if(currentPlaylist!!.get().hasPreviousBefore(currentlyPlayingSong!!))
                {
                    val currentIndex:Int = currentPlaylist!!.get().songsList!!.indexOf(currentlyPlayingSong)
                    myMediaPlayer.reset()
                    this.setSong(currentPlaylist!!.get().songsList!![currentIndex-1])
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


    fun setSong(song:Song,playlist: AtomicReference<Playlist>?=null) {

        val savedCurrentPosition = myMediaPlayer.mediaplayer.currentPosition

        initializeMediaPlayer()
        if (myMediaPlayer.iPrepared_)
            myMediaPlayer.reset()

        try {
            if (!mediaplayer.isPlaying){
                Log.i("TESTS","Set song requested")
                try {
                    mediaplayer.apply {
                        Application.instance.applicationContext.contentResolver.openFileDescriptor(
                            song.from(SongsGlobalVars.allSongs)!!.songUri.toUri(),
                            "r"
                        ).use { pfd ->
                            isPrepared = false
                            setDataSource(pfd?.fileDescriptor)
                            prepareAsync()


                            mediaplayer.setOnPreparedListener {
                                isPrepared = true


                                val lastSong = currentlyPlayingSong
                                currentlyPlayingSong = song.from(SongsGlobalVars.allSongs)

                                Log.i(
                                    "TESTS",
                                    "Set song approved for song URI [${currentlyPlayingSong?.songUri}]"
                                )

                                bus.post(Events.SongWasChanged(lastSong, currentlyPlayingSong))
                                _Playing = true


                                // update the stats too
                                song.from(SongsGlobalVars.allSongs)?.timesListened++
                                song.from(SongsGlobalVars.allSongs)?.lastPlayed =
                                    LocalDateTime.now().toString()


                                SongsGlobalVars.RecentlyPlayed.apply {
                                    if(this.songsList!=null) {
                                        if (this.songsList!!.isNotEmpty() && this.songsList!!.contains(
                                                song.from(SongsGlobalVars.allSongs)
                                            )
                                        ) {
                                            this.songsList!!.remove(song.from(SongsGlobalVars.allSongs))
                                        }
                                        this.add(song.from(SongsGlobalVars.allSongs)!!)
                                    }
                                }



                                if(playlist!=null){
                                    myMediaPlayer.openPlaylist(playlist)
                                }

                                myMediaPlayer.start()
                            }

                            Log.i(
                                Logs.MEDIA_SOUND.toString(),
                                "Mediaplayer song set to ${currentlyPlayingSong?.title}"
                            )


                        }

                    }
                }catch (exception: FileNotFoundException){
                    Log.e(Logs.FILE_IO.name,"File not found while setting song!",exception.cause)
                    if(currentlyPlayingSong!=null) {
                        myMediaPlayer.setSong(currentlyPlayingSong?.from(SongsGlobalVars.allSongs)!!)
                        myMediaPlayer.seekTo(savedCurrentPosition.toLong())
                    }

                }


            }
        }catch (ex: IllegalStateException){
            Log.e(Logs.MEDIA_SOUND.toString(), "Cannot set song when mediaplayer is not initialized. FROM SET SONG")
        }
    }

    fun start() {

        if(currentlyPlayingSong!=null && !isPlaying && isPrepared) {
            Log.i("TESTS","Start song requested")
            mediaplayer.start()
            _Playing=true
            bus.post(Events.SongWasStarted())
        }

    }

     fun stop() {
        if(currentlyPlayingSong!=null && isPrepared) {
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

     fun getCurrentPosition(): Long {

        if(myMediaPlayer.currentlyPlayingSong!=null)
            return try{
                mediaplayer.currentPosition.toLong()
            }catch(ex: Exception){
                ex.printStackTrace()
                0
            }
        else
            return 0
    }



    fun reset() {

        if(isInitialized ) {

            if (currentlyPlayingSong != null && isPrepared) {
                isPrepared=false
                mediaplayer.reset()
                _Playing=false
                bus.post(Events.SongWasReset())
            }
        }
    }

     fun release() {
        Log.i("TESTS","MEDIAPLAYER RELEASE requested")
        isPrepared=false
        isInitialized=false
        _Playing=false
        currentPlaylist=null
        currentlyPlayingSong=null
        mediaplayer.release()
    }







     fun pause(){
        if(currentlyPlayingSong!=null && isPrepared) {
            mediaplayer.pause()
            _Playing=false
            bus.post(Events.SongWasPaused())

        }
    }








}