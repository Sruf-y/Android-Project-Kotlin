package SongsMain.Tutorial


import DataClasses_Ojects.MediaProgressViewModel
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import SongsMain.SongMain_Activity
import SongsMain.SongsMain_Base
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.example.composepls.R
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.security.Provider
import kotlin.getValue

class MusicPlayerService: Service() {


    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    val bus = EventBus.getDefault()
    val binder = MusicBinder()
    private lateinit var mediaSession: MediaSessionCompat
    inner class MusicBinder:Binder(){

    }
    companion object {
        private var isRunning = false

        fun isServiceRunning() = isRunning
    }







    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    val NOTIFICATION_ID = 105


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            MediaPlayerServiceActions.SERVICESTART.name -> serviceStart()
            MediaPlayerServiceActions.SERVICESTOP.name -> serviceStop()
            MediaPlayerServiceActions.TOGGLE.name -> myMediaPlayer.toggle()
            MediaPlayerServiceActions.BACKWARD.name -> { /* Handle going back */ }
            MediaPlayerServiceActions.FORWARD.name -> { /* Handle skip */ }
            MediaPlayerServiceActions.SEEK.name -> {
                val position = intent.getIntExtra("position", 0)
                myMediaPlayer.mediaplayer.seekTo(position)
            }
            MediaPlayerServiceActions.OPENAPP.name -> { /* Optional: open app intent */ }
        }


        return START_NOT_STICKY
    }




    private fun playPausePendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = MediaPlayerServiceActions.TOGGLE.name
        }
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun prevPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = MediaPlayerServiceActions.BACKWARD.name
        }
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun nextPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = MediaPlayerServiceActions.FORWARD.name
        }
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }






     fun CreateNotification(song:Song?=null){


         val play_pause_icon = if (myMediaPlayer._Playing || myMediaPlayer.isPlaying) {
             R.drawable.pause_button_music_player
         } else {
             R.drawable.play_button_music_player
         }

         val SONG = if (song != null) {
             song
         } else {
             myMediaPlayer.currentlyPlayingSong
         }


        CoroutineScope(Dispatchers.Default).launch {
            val song_art = Functions.Images.loadFromFile(File(File(Application.instance.filesDir,"MusicDir"),
                SONG?.songUri?.toUri()?.lastPathSegment.toString() +".jpg"))


            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, myMediaPlayer.currentlyPlayingSong?.title ?: "<unknown>")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,myMediaPlayer.currentlyPlayingSong?.title ?: "<unknown>")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, myMediaPlayer.mediaplayer.duration.toLong())
                .apply {
                    if(song_art!=null){
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                        song_art)
                    }
                }
                .build()

            mediaSession.setMetadata(metadata)













            val notification = NotificationCompat.Builder(this@MusicPlayerService, SongsGlobalVars.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_app_icon)
                .setDeleteIntent(getStopServicePendingIntent())
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2,3))
                //.setProgress()
                .addAction(R.drawable.skip_to_previous, "Previous", prevPendingIntent())
                .addAction(play_pause_icon, "Play/Pause", playPausePendingIntent())
                .addAction(R.drawable.skip_to_next, "Next", nextPendingIntent())
                .addAction(R.drawable.x,"Close",getStopServicePendingIntent())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .apply {
                    if(song_art!=null)
                    {
                        setLargeIcon(song_art)
                    }
                }
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }






    override fun onCreate() {
        super.onCreate()
        isRunning = true
        bus.register(this)

        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = myMediaPlayer.start()
                override fun onPause() = myMediaPlayer.pause()
                override fun onSeekTo(pos: Long) = Unit
                override fun onSkipToPrevious() = Unit
                override fun onSkipToNext() = Unit
                override fun onRewind() = Unit
            })
            isActive = true
        }

        // stuff about audio focus
        myMediaPlayer.mediaplayer.pause()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()







        CreateNotification()
    }

    fun onEvent(event:Events.SongWasChanged){
        CreateNotification(event.currentSong)
        Log.i("TESTS","(Service) Notitifaction was created once")
    }



    fun onEvent(event:Events.SongWasPaused){
        CreateNotification()
    }


    //
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                myMediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> myMediaPlayer.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> myMediaPlayer.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> myMediaPlayer.setVolume(1.0f, 1.0f)
        }
    }

    fun onEvent(event:Events.SongWasStarted){

        myMediaPlayer.mediaplayer.pause()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()

        val result = audioManager.requestAudioFocus(audioFocusRequest)

        if(result== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            myMediaPlayer.mediaplayer.start()
        }

        CreateNotification(myMediaPlayer.currentlyPlayingSong)
    }




    override fun onDestroy() {
        isRunning=false
        bus.unregister(this)
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        if(!SongMain_Activity.ActiveTracker.isRunningAnywhere)
        {
            myMediaPlayer.release()
        }
        super.onDestroy()
    }



    private fun serviceStart() {
        CreateNotification()
    }

    private fun serviceStop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun getStopServicePendingIntent(): PendingIntent {
        isRunning=false

        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = MediaPlayerServiceActions.SERVICESTOP.name
        }
        return PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    enum class MediaPlayerServiceActions{
        SERVICESTART,
        SERVICESTOP,
        TOGGLE,
        FORWARD,
        BACKWARD,
        SEEK,
        OPENAPP
    }




}