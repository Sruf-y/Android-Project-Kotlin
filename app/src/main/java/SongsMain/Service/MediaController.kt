package SongsMain.Classes


import SongsMain.Tutorial.Application
import SongsMain.Tutorial.MusicPlayerService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import android.content.Intent

@OptIn(UnstableApi::class)
object MyMediaController {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val listeners = mutableListOf<Player.Listener>()

    private const val TAG = "MyMediaController"

    fun initialize(context: Context) {
        if (controllerFuture != null || mediaController != null) {
            Log.d(TAG, "Controller already initialized")
            return
        }

        // First ensure the service is running
        startServiceIfNeeded(context)

        // Then create the controller
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlayerService::class.java))

        controllerFuture = MediaController.Builder(context, sessionToken)
            .buildAsync()

        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                Log.d(TAG, "MediaController successfully initialized")

                // Add all registered listeners to the new controller
                listeners.forEach { listener ->
                    mediaController?.addListener(listener)
                }

                // Notify that controller is ready
                onControllerReady()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaController", e)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startServiceIfNeeded(context: Context) {
        // Start the service if it's not running
        if (!MusicPlayerService.isServiceRunning()) {
            Log.d(TAG, "Starting Media3Service")
            ContextCompat.startForegroundService(
                context,
                Intent(context, MusicPlayerService::class.java).apply {
                    action = "ACTION_START_FOREGROUND"
                }
            )

            // Short delay to ensure service has time to start
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    private fun onControllerReady() {
        // This can be used to notify any components waiting for the controller
        // For example, you could use EventBus to post an event
        // EventBus.getDefault().post(Events.MediaControllerReady())
    }

    fun addListener(listener: Player.Listener) {
        // Add to our local list
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }

        // Also add to active controller if available
        mediaController?.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
        mediaController?.removeListener(listener)
    }

    // Forward common player commands to the controller

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleFavorite() {
        // Send custom command to the service
        sendCustomCommand("ACTION_FAVORITE")
    }

    private fun sendCustomCommand(action: String) {
        val context = Application.instance
        ContextCompat.startForegroundService(
            context,
            Intent(context, MusicPlayerService::class.java).apply {
                this.action = action
            }
        )
    }

    // Access to common player states

    val isPlaying: Boolean
        get() = mediaController?.isPlaying ?: false

    val currentPosition: Long
        get() = mediaController?.currentPosition ?: 0

    val duration: Long
        get() = mediaController?.duration ?: 0

    val currentMediaItemIndex: Int
        get() = mediaController?.currentMediaItemIndex ?: -1

    fun release() {
        listeners.clear()
        mediaController?.release()
        mediaController = null
        controllerFuture?.cancel(true)
        controllerFuture = null
    }
}