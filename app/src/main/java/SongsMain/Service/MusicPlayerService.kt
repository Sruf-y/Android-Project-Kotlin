package SongsMain.Tutorial


import DataClasses_Ojects.Logs
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.Song.Companion.from
import SongsMain.Classes.myExoPlayer
import SongsMain.SongMain_Activity
import SongsMain.SongsMain_Base
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Variables.SongsGlobalVars.CHANNEL_ID
import SongsMain.Variables.SongsGlobalVars.SongsOperations.setFavorite
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import com.example.composepls.R
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.media3.common.util.UnstableApi
import java.io.FileNotFoundException


@OptIn(UnstableApi::class)
class MusicPlayerService: Service() {


    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    val bus = EventBus.getDefault()
    val binder = MusicBinder()

    inner class MusicBinder:Binder(){

    }
    companion object {

        fun updateNotificationMusic(){

        }

        enum class MediaPlayerServiceActions{
            SERVICESTART,
            SERVICESTOP,
            TOGGLE,
            FORWARD,
            BACKWARD,
            SEEK,
            OPENAPP,
            FAVORITE
        }

        private var isRunning = false

        fun isServiceRunning() = isRunning

        lateinit var mediaSession: MediaSessionCompat
    }







    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    val NOTIFICATION_ID = 105


    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            MediaPlayerServiceActions.SERVICESTART.name -> serviceStart()
            MediaPlayerServiceActions.SERVICESTOP.name -> {
                serviceStop()
                return START_NOT_STICKY}
            MediaPlayerServiceActions.TOGGLE.name -> myExoPlayer.toggle()
            MediaPlayerServiceActions.BACKWARD.name -> {
                myExoPlayer.playPreviousInPlaylist()
            }
            MediaPlayerServiceActions.FORWARD.name -> {
                myExoPlayer.playNextInPlaylist()
            }
            MediaPlayerServiceActions.SEEK.name -> {
                val position = intent.getIntExtra("position", 0)
                myExoPlayer.seekTo(position.toLong())
            }
            MediaPlayerServiceActions.OPENAPP.name -> { /* Optional: open app intent */ }



            else -> {
                myExoPlayer.initializePlayer(this)

                // Re-initialize metadata/session if needed

                setupAudioManagerRequest()


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


    @OptIn(UnstableApi::class)
    fun CreateNotification(song:Song?=null,setSongAsStarted:Boolean=false){




        val SONG = if (song != null) {
            song
        } else {
            myExoPlayer.currentlyPlayingSong
        }




            CoroutineScope(Dispatchers.Main).launch {

                var favorite_drawable = R.drawable.favorite_heart_unchecked

                if(SONG!=null){
                    if(SONG.isFavorite){
                        favorite_drawable=R.drawable.favorite_heart_checked
                    }
                }

                var file:File? = null
                var song_art:Bitmap? = null
                try {
                    if(SONG?.thumbnail!=null) {
                        file = File(SONG?.thumbnail)
                        if (file.exists()) {
                            val bytes = file.readBytes()

                            song_art = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                    }


                }catch (ex: NullPointerException){
                    Log.e(Logs.ERRORS.name,"Song_art for notification threw a nullpointer 'file' is [${file}].",ex)
                }catch (ex: FileNotFoundException){
                    Log.e(Logs.ERRORS.name,"Song thumbnail file not found, path was ${SONG?.thumbnail}",ex)
                }

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, myExoPlayer.currentlyPlayingSong?.title ?: "<unknown>")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,myExoPlayer.currentlyPlayingSong?.author ?: "<unknown>")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                    myExoPlayer.currentlyPlayingSong?.duration ?: 0
                )
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
                        if (myExoPlayer.isPlaying || setSongAsStarted) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                        myExoPlayer.getCurrentPosition(),
                        1.0f
                    )

                    .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                    .addCustomAction(MediaPlayerServiceActions.FAVORITE.name,"favorite_button",favorite_drawable)
                    .setActions(
                        PlaybackStateCompat.ACTION_SEEK_TO or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                    .addCustomAction(MediaPlayerServiceActions.SERVICESTOP.name,"x_button", R.drawable.x)
                    .build()
            )


            mediaSession.setMetadata(metadata)












            val notification = NotificationCompat.Builder(this@MusicPlayerService, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_app_icon)
                .setDeleteIntent(getStopServicePendingIntent())

                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                )

                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build()


                startForeground(NOTIFICATION_ID, notification)


        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {

        SongMain_Activity.isRunningAnywhere=false
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()



        isRunning = true
        bus.register(this)


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






        // callback, here you can handle default actions and CUSTOM ACTIONS

        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        MediaPlayerServiceActions.SERVICESTOP.name -> {

                            stopForeground(STOP_FOREGROUND_REMOVE)
                            if (!SongMain_Activity.isRunningAnywhere) {
                                serviceStop()
                            }

                        }

                        MediaPlayerServiceActions.FAVORITE.name -> {
                            myExoPlayer.currentlyPlayingSong?.isFavorite?.let {

                                // this SHOULD save the favorite state
                                myExoPlayer.currentlyPlayingSong?.setFavorite(!it)

                                CreateNotification()



                            }
                        }
                    }
                }

                override fun onPlay() {
                    myExoPlayer.start()
                    CreateNotification()
                }

                override fun onPause() {
                    myExoPlayer.pause()
                    CreateNotification()
                }

                override fun onSeekTo(pos: Long) {
                    myExoPlayer.isPlaying.apply {

                        myExoPlayer.seekTo(pos)
                        CreateNotification(setSongAsStarted = this)
                    }


                }

                override fun onSkipToPrevious() {

                        myExoPlayer.playPreviousInPlaylist()

                        CreateNotification(setSongAsStarted = true)

                }
                override fun onSkipToNext()  {
                    myExoPlayer.playNextInPlaylist()
                    CreateNotification(setSongAsStarted = true)
                }
                override fun onRewind()  { myExoPlayer.rewind() }
            })
            isActive = true
        }


        // stuff about audio focus (also throws exception if i pause before its reinitialized sooo)
        myExoPlayer.initializePlayer(this)
        setupAudioManagerRequest()

        //CreateNotification()


        //myExoPlayer.mediaplayer.pause()






        onEvent(Events.SongWasStarted())







    }




    private fun setupAudioManagerRequest(){

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
                myExoPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> myExoPlayer.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> myExoPlayer.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> myExoPlayer.setVolume(1.0f, 1.0f)
        }
    }

    fun onEvent(event:Events.SongWasStarted){


        setupAudioManagerRequest()
        val result = audioManager.requestAudioFocus(audioFocusRequest)

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Only update notification after focus is granted
            CreateNotification(myExoPlayer.currentlyPlayingSong, true)
        } else {
            // Handle the case where focus was denied
            Log.w("AudioFocus", "Audio focus request was denied")
            // You might want to pause playback here
        }


    }




    override fun onDestroy() {
        Log.i("WTF","Service has been DESTROYED (onDestroy)")
        super.onDestroy()
    }



    private fun serviceStart() {
        CreateNotification()
    }

    private fun serviceStop() {
        isRunning=false
        bus.unregister(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i("TESTS","Service has STOPPED (serviceStop)")
        audioManager.abandonAudioFocusRequest(audioFocusRequest)

        if(!SongMain_Activity.ActiveTracker.isRunningAnywhere)
        {
            myExoPlayer.release()

        }
        stopSelf()

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






}