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
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.example.composepls.R
import com.google.common.util.concurrent.Futures
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

    private lateinit var notificationManager: PlayerNotificationManager





    override fun onCreate() {
        super.onCreate()

//        fun addToInvalidateQueue(action:(()-> Unit)){
//            CoroutineScope(Dispatchers.IO).launch {
//                actionBuffer.withLock {
//
//
//
//                    CoroutineScope(Dispatchers.Main).launch {
//                        action.invoke()
//                        notificationManager.invalidate()
//                    }
//                }
//            }
//        }


        // 1. Initialize ExoPlayer
        myExoPlayer.initializePlayer(Application.instance)
        val exoPlayer = myExoPlayer.exoPlayer


        myExoPlayer.exoPlayer!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
            }

        })


        val forwardingPlayer = object : ForwardingPlayer(exoPlayer!!) {

            // override default buttons actions
            override fun seekToPrevious() {

                 myExoPlayer.playPreviousInPlaylist()


            }


            override fun seekToNextMediaItem() {

                myExoPlayer.playNextInPlaylist()



            }



            override fun play() {

                myExoPlayer.start()

            }

            override fun pause() {

                myExoPlayer.pause()
                //notificationManager.invalidate()
            }

            override fun getAvailableCommands(): Player.Commands {
                val commands = Player.Commands.Builder()
                    .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_PLAY_PAUSE)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)

                    .build()


                return commands
            }

        }




        val closebutton =
            CommandButton.Builder()
                .setIconResId(R.drawable.x)
                .setSessionCommand(SessionCommand(CLOSE_NOTIFICATION, Bundle.EMPTY))
                .build()

        // 2. Create MediaSession
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setCallback(CustomMediaSessionCallback())
            .setCustomLayout(listOf<CommandButton>(closebutton)) // set the ORDER of buttons i think
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

            setUsePlayPauseActions(false)
            setUseNextAction(false)
            setUsePreviousAction(false)
            setUseStopAction(false)


            setUseChronometer(true)
        }





        bus.apply {
            if(!isRegistered(this@Media3Service)){
                register(this@Media3Service)
            }
        }


    }

    fun onEvent(event:Events.SongWasChanged){

        notificationManager.setPlayer(exoPlayer)

    }




    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "ACTION_CLOSE" -> {
                    stopSelf()
                    return START_NOT_STICKY
                }
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
        lateinit var mediaSession: MediaSession

        val actionBuffer = Mutex()
    }


    val CLOSE_NOTIFICATION = "close_notification"

    inner class CustomMediaSessionCallback: MediaSession.Callback {
        // Configure commands available to the controller in onConnect()
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CLOSE_NOTIFICATION, Bundle.EMPTY))

                .build()
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
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
            val cancelIntent = PendingIntent.getService(
                context,-1,
                Intent(context,Media3Service::class.java).apply {
                    action = "ACTION_CUSTOM_CANCEL"
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val playIntent = PendingIntent.getService(
                context, 0,
                Intent(context, Media3Service::class.java).apply { action = "ACTION_CUSTOM_PLAY" },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val pauseIntent = PendingIntent.getService(
                context, 1,
                Intent(context, Media3Service::class.java).apply { action = "ACTION_CUSTOM_PAUSE" },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val cancelAction = NotificationCompat.Action(
                R.drawable.x,"Cancel",cancelIntent
            )

            val playAction = NotificationCompat.Action(
                R.drawable.play_button_music_player, "Play", playIntent
            )
            val pauseAction = NotificationCompat.Action(
                R.drawable.pause_button_music_player, "Pause", pauseIntent
            )

            return mapOf(
                "custom_play" to playAction,
                "custom_pause" to pauseAction,
            )
        }

        override fun getCustomActions(player: Player): MutableList<String> {
                return mutableListOf("custom_pause","custom_play","custom_cancel")
        }

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
            when (action) {
                "custom_play" -> {
                    myExoPlayer.start()
                    notificationManager.invalidate()
                }
                "custom_pause" -> {
                    myExoPlayer.pause()
                    notificationManager.invalidate()
                }
                "custom_cancel"->{
                    stopSelf()
                }
            }
        }
    }





}


