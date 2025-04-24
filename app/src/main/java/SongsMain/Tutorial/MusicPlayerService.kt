package SongsMain.Tutorial


import DataClasses_Ojects.Logs
import DataClasses_Ojects.MediaProgressViewModel
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import SongsMain.SongMain_Activity
import SongsMain.SongsMain_Base
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import androidx.core.graphics.createBitmap

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
            MediaPlayerServiceActions.SERVICESTOP.name -> {
                serviceStop()
                return START_NOT_STICKY}
            MediaPlayerServiceActions.TOGGLE.name -> myMediaPlayer.toggle()
            MediaPlayerServiceActions.BACKWARD.name -> {
                myMediaPlayer.playPreviousInPlaylist()
            }
            MediaPlayerServiceActions.FORWARD.name -> {
                myMediaPlayer.playNextInPlaylist()
            }
            MediaPlayerServiceActions.SEEK.name -> {
                val position = intent.getIntExtra("position", 0)
                myMediaPlayer.mediaplayer.seekTo(position)
            }
            MediaPlayerServiceActions.OPENAPP.name -> { /* Optional: open app intent */ }



        else -> {
            if (myMediaPlayer.isInitialized_) {
                myMediaPlayer.initializeMediaPlayer()
            }

            // Re-initialize metadata/session if needed

                setupMediaSession()


            isRunning = true
        }
    }

    return START_STICKY
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
    private fun getOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, SongMain_Activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }



     fun CreateNotification(song:Song?=null,setSongAsStarted:Boolean=false){


         val play_pause_icon = if (myMediaPlayer._Playing || myMediaPlayer.isPlaying || setSongAsStarted) {
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




            val song_art = Functions.Images.loadFromFile(File(SONG?.thumbnail?:""))


            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, myMediaPlayer.currentlyPlayingSong?.title ?: "<unknown>")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,myMediaPlayer.currentlyPlayingSong?.author ?: "<unknown>")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, myMediaPlayer.mediaplayer.duration.toLong())
                .apply {

                    if(song_art!=null){
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                        song_art)
                    }
                    else{
                        val bitmap =  Functions.Images.getBitmapFromDrawable(R.drawable.blank_gray_musical_note)
                        Log.i(Logs.MEDIA_IMAGES.name,"Value of song_art is ${bitmap.toString()}")
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ART,bitmap)
                    }
                }
                .build()

            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        if (myMediaPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                        myMediaPlayer.getCurrentPosition().toLong(),
                        1.0f
                    )
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_SEEK_TO or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_REWIND or
                                PlaybackStateCompat.ACTION_STOP
                    )
                    .build()
            )


            mediaSession.setMetadata(metadata)













            val notification = NotificationCompat.Builder(this@MusicPlayerService, SongsGlobalVars.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_app_icon)
                .setDeleteIntent(getStopServicePendingIntent())
                .setContentIntent(getOpenAppPendingIntent())
                .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(getStopServicePendingIntent())

                )
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


    override fun onTaskRemoved(rootIntent: Intent?) {
        //serviceStop()
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()



        isRunning = true
        bus.register(this)

        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = myMediaPlayer.start()
                override fun onPause() = myMediaPlayer.pause()
                override fun onSeekTo(pos: Long)  = myMediaPlayer.seekTo(pos)
                override fun onSkipToPrevious() = myMediaPlayer.playPreviousInPlaylist()
                override fun onSkipToNext() = myMediaPlayer.playNextInPlaylist()
                override fun onRewind() = myMediaPlayer.rewind()
                override fun onStop() {
                    super.onStop()
                    serviceStop()
                }

            })
            isActive = true
        }



        // stuff about audio focus (also throws exception if i pause before its reinitialized sooo)
        myMediaPlayer.initializeMediaPlayer()



            //myMediaPlayer.mediaplayer.pause()
            onEvent(Events.SongWasStarted())
            CreateNotification()







    }


    private fun setupMediaSession(){

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

        //myMediaPlayer.mediaplayer.pause()
        setupMediaSession()

        val result = audioManager.requestAudioFocus(audioFocusRequest)

        if(result== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            myMediaPlayer.mediaplayer.start()
        }
        else{
            myMediaPlayer.pause()
        }

        CreateNotification(myMediaPlayer.currentlyPlayingSong,true)
    }
    fun onEvent(event:Events.SongWas_UsedSeek){
        CreateNotification()
    }




    override fun onDestroy() {
        Log.i("TESTS","Service has been DESTROYED (onDestroy)")
        super.onDestroy()
    }



    private fun serviceStart() {
        CreateNotification()
    }

    private fun serviceStop() {
        isRunning=false
        bus.unregister(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i("TESTS","Service has STOPPED (serviceStop) and app running is ${SongMain_Activity.ActiveTracker.isRunningAnywhere}")
        audioManager.abandonAudioFocusRequest(audioFocusRequest)

        if(!SongMain_Activity.ActiveTracker.isRunningAnywhere)
        {
            myMediaPlayer.release()
            stopSelf()
        }


    }

    private fun getStopServicePendingIntent(): PendingIntent {
        Log.i("TESTS","Service has been sent stop pending intent (getStopServicePendingIntent)")
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