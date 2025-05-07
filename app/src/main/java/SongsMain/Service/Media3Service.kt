package SongsMain.Service

import Functions.Images
import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.myExoPlayer
import SongsMain.Classes.myExoPlayer.exoPlayer
import SongsMain.SongMain_Activity
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars.CHANNEL_ID
import SongsMain.Variables.SongsGlobalVars.CHANNEL_NAME
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.example.composepls.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import de.greenrobot.event.EventBus
import java.io.File


@OptIn(UnstableApi::class)
class Media3Service : MediaSessionService() {

    val bus = EventBus.getDefault()

    private lateinit var notificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()



        // 1. Initialize ExoPlayer
        myExoPlayer.initializePlayer(Application.instance)
        val exoPlayer = myExoPlayer.exoPlayer

        // 2. Create MediaSession
        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .setCallback(MediaSessionCallback())
            .build()

        // 3. Configure Notification Manager
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        ).apply {
            setMediaDescriptionAdapter(createMediaDescriptionAdapter())
            setSmallIconResourceId(R.drawable.music_app_icon)
            setNotificationListener(NotificationListener())
            //custom actions
            setCustomActionReceiver(CustomActionReceiver())

        }.build().apply {
            setPlayer(exoPlayer)
            setMediaSessionToken(mediaSession.sessionCompatToken)

            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setUsePreviousAction(true)
            setUseNextAction(true)
            setUseStopAction(true)


            setUseChronometer(true)
        }





        bus.apply {
            if(!isRegistered(this@Media3Service)){
                register(this@Media3Service)
            }
        }


    }

    fun onEvent(event:Events.SongWasStarted){
        notificationManager.invalidate()


    }




    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        intent?.action.let { action ->
            when (action) {
                "ACTION_CLOSE" -> {
                    stopSelf()
                    return START_NOT_STICKY
                }
                "ACTION_START_FOREGROUND" -> {
                    // The PlayerNotificationManager will update this automatically
                    return START_STICKY
                }
                null -> {
                    // Explicitly handle null intent
                        Log.d("Service", "Received null intent - ignoring")
                    super.onStartCommand(intent, flags, startId)
                    }
                else->super.onStartCommand(intent, flags, startId)
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

    private inner class CustomActionReceiver : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(
            context: Context,
            instanceId: Int
        ): Map<String, NotificationCompat.Action> {

            val closeAction = NotificationCompat.Action(
                R.drawable.x, // Your close icon
                "Close",
                PendingIntent.getService(
                    context,
                    0,
                    Intent(context, Media3Service::class.java).apply {
                        action = "ACTION_CLOSE"
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            return mapOf("custom_close" to closeAction)
        }

        override fun getCustomActions(player: Player): MutableList<String> {
            return mutableListOf("custom_close") // Always show close button
        }

        override fun onCustomAction(
            player: Player,
            action: String,
            intent: Intent
        ) {
            when(action) {
                "custom_close" -> {
                    // Handle close action

                    stopSelf()

                    // Post event if needed
                    //EventBus.getDefault().post(Events.NotificationClosed())}
                }
            }
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                startForeground(notificationId, notification)
            }
        }

        override fun onNotificationCancelled(
            notificationId: Int,
            dismissedByUser: Boolean
        ) {
            if (dismissedByUser) {
                stopSelf()
            }
        }
    }



    override fun onDestroy() {
        notificationManager.setPlayer(null)
        mediaSession.release()

        bus.apply {
            if(isRegistered(this@Media3Service)){
                unregister(this@Media3Service)
            }
        }
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 110
        var isServiceRunning = false
    }










    private inner class MediaSessionCallback : MediaSession.Callback {
        // Playback Controls

         fun onPlay(session: MediaSession, controller: ControllerInfo) {
            myExoPlayer.start()
            EventBus.getDefault().post(Events.SongWasStarted())
        }

         fun onPause(session: MediaSession, controller: ControllerInfo) {
            myExoPlayer.pause()
            EventBus.getDefault().post(Events.SongWasPaused())
        }

         fun onStop(session: MediaSession, controller: ControllerInfo) {
            stopSelf()
            EventBus.getDefault().post(Events.SongWasStopped())
        }

        // Playlist Navigation
         fun onSkipToNext(session: MediaSession, controller: ControllerInfo) {
            myExoPlayer.playNextInPlaylist() // Your existing method
        }

        fun onSkipToPrevious(session: MediaSession, controller: ControllerInfo) {
            myExoPlayer.playPreviousInPlaylist() // Your existing method
        }

        // Seeking
         fun onSeekTo(session: MediaSession, controller: ControllerInfo, positionMs: Long) {
            myExoPlayer.seekTo(positionMs)
        }

        // Custom Actions (e.g., your close button)
        override fun onCustomCommand(
            session: MediaSession,
            controller: ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == "ACTION_CLOSE") {
                stopSelf()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}


