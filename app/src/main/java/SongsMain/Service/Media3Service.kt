package SongsMain.Service

import Functions.Images
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.myExoPlayer
import SongsMain.Classes.myExoPlayer.exoPlayer
import SongsMain.Service.MusicPlayerService
import SongsMain.SongMain_Activity
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Variables.SongsGlobalVars.CHANNEL_ID
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.example.composepls.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import de.greenrobot.event.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File


@OptIn(UnstableApi::class)
class Media3Service : MediaSessionService() {

    val bus = EventBus.getDefault()

    private var notificationManager: PlayerNotificationManager?=null

    fun addToActionQueue(action:(()->Unit)){
        CoroutineScope(Dispatchers.Main).launch {
            actionBuffer.withLock {
                action.invoke()
                delay(300)
            }
        }
    }


    fun createAuxiliaryNotification(){
        val notification = NotificationCompat.Builder(this, SongsGlobalVars.CHANNEL_ID)
            .setSmallIcon(R.drawable.music_app_icon)

            .setContentTitle("No song selected")
            .setOngoing(true)
            .setOnlyAlertOnce(true)

            .build()


        startForeground(NOTIFICATION_ID, notification)

    }


    override fun onCreate() {
        super.onCreate()

        createAuxiliaryNotification()


        // 1. Initialize ExoPlayer
        myExoPlayer.initializePlayer(Application.instance)
        val exoPlayer = myExoPlayer.exoPlayer


        // 1.5 Wrap player to override commands called by notification default actions
        val forwardingPlayer = object : ForwardingPlayer(exoPlayer!!) {

            // override default buttons actions
            override fun seekToPrevious() {

                addToActionQueue {
                    if(myExoPlayer.getCurrentPosition()>3000){
                        myExoPlayer.seekTo(0)
                    }else
                        myExoPlayer.playPreviousInPlaylist()
                }

            }


            override fun seekToNextMediaItem() {

                addToActionQueue { myExoPlayer.playNextInPlaylist() }

            }



            override fun play() {

                myExoPlayer.start()

            }

            override fun pause() {

                myExoPlayer.pause()


            }



            override fun getAvailableCommands(): Player.Commands {
                val commands = Player.Commands.Builder()
                    .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    .addIf(COMMAND_SEEK_TO_PREVIOUS, myExoPlayer.currentPlaylist?.get()?.hasPreviousBefore(
                        myExoPlayer.currentlyPlayingSong!!) == true
                    )
                    .add(COMMAND_PLAY_PAUSE)
                    .addIf(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM, myExoPlayer.currentPlaylist?.get()?.hasNextAfter(
                        myExoPlayer.currentlyPlayingSong!!) == true
                    )

                    .build()


                return commands
            }


        }






        // 2. Create MediaSession
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .build()

        // 3. Configure Notification Manager


        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        ).apply {
            setMediaDescriptionAdapter(createMediaDescriptionAdapter())
            setSmallIconResourceId(R.drawable.music_app_icon)


        }.build().apply {
            setPlayer(exoPlayer)
            setMediaSessionToken(mediaSession.platformToken)

            setUsePlayPauseActions(false)
            setUseNextAction(false)
            setUsePreviousAction(false)
            setUseStopAction(false)






        }








        bus.apply {
            if(!isRegistered(this@Media3Service)){
                register(this@Media3Service)
            }
        }


    }

    fun onEvent(event:Events.SongWasChanged){



    }




    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "ACTION_START_FOREGROUND" -> {
                    // Your custom action to start the notification
                    return START_STICKY
                }
                else -> {
                    super.onStartCommand(intent, flags, startId)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }









    private fun createMediaDescriptionAdapter() =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return (player.currentMediaItem?.mediaMetadata?.title
                    ?: (player.currentMediaItem?.localConfiguration?.tag as? Song)?.title
                    ?: "Unknown")
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                // Create intent to open your player activity when notification is clicked
                return PendingIntent.getActivity(
                    this@Media3Service,
                    0,
                    Intent(this@Media3Service, SongMain_Activity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("notification_clicked", true)
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return (player.currentMediaItem?.mediaMetadata?.artist
                    ?: (player.currentMediaItem?.localConfiguration?.tag as? Song)?.author
                    ?: "Unknown Artist")
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val song = player.currentMediaItem?.localConfiguration?.tag as? Song
                return song?.thumbnail?.let { uri ->
                    Images.loadFromFileinSync(File(uri)) // Your existing image loader
                } ?: Images.getBitmapFromDrawable(R.drawable.blank_gray_musical_note)
            }
        }


    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        mediaSession.release()

        bus.apply {
            if(isRegistered(this@Media3Service)){
                unregister(this@Media3Service)
            }
        }
        stopSelf()
        isServiceRunning=false
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 110
        var isServiceRunning = false
        lateinit var mediaSession: MediaSession

        val actionBuffer = Mutex()


    }



}

