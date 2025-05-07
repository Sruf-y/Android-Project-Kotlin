package SongsMain.Service

import android.R.attr.action
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture


object MyMediaController {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val listeners = mutableListOf<Player.Listener>()

    fun initialize(context: Context) {
        if (controllerFuture != null || mediaController != null) return

        // Ensure the service is running BEFORE connecting the controller
        if (!Media3Service.isServiceRunning) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, Media3Service::class.java).apply {
                    action = "ACTION_START_FOREGROUND"
                }
            )
            return // Wait for the service to fully initialize before connecting
        }

        controllerFuture = MediaController.Builder(
            context,
            Media3Service.mediaSession.token
        ).buildAsync().apply {
            addListener({
                try {
                    mediaController = get().apply {
                        listeners.forEach { addListener(it) }
                    }
                } catch (e: Exception) {
                    Log.e("MediaController", "Initialization failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }



    fun getController(): MediaController? = mediaController

    fun addListener(listener: Player.Listener) {
        listeners.add(listener)
        mediaController?.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
        mediaController?.removeListener(listener)
    }

    fun release() {
        controllerFuture?.cancel(true)
        mediaController?.run {
            listeners.forEach { removeListener(it) }
            release()
        }
        mediaController = null
        controllerFuture = null
        listeners.clear()
    }
}