package SongsMain.Service

import android.content.ComponentName
import android.content.Context
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
        if (controllerFuture != null) return

        controllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, Media3Service::class.java))
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