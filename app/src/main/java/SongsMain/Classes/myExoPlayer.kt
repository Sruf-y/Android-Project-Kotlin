package SongsMain.Classes

import DataClasses_Ojects.Logs
import SongsMain.Classes.Song.Companion.from
import SongsMain.Classes.Song.Companion.toMediaItem
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import de.greenrobot.event.EventBus
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

@UnstableApi
object myExoPlayer {
    private val bus: EventBus = EventBus.getDefault()
     var exoPlayer: ExoPlayer? = null
     var isInitialized = false
     var isPrepared = false

    var currentlyPlayingSong: Song? = null
    var currentPlaylist: AtomicReference<Playlist>? = null
    var isLoopingInPlaylist: Boolean = false

    // Player state tracking
    private var _isPlaying = false
    var isPlaying: Boolean
        get() = exoPlayer?.isPlaying ?: _isPlaying
        set(value) { _isPlaying = value }

    fun initializePlayer(context: Context) {
        if (!isInitialized) {
            exoPlayer = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    androidx.media3.common.AudioAttributes.DEFAULT,
                    true // Handle audio focus
                )
                .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
                .build()
                .apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> isPrepared = true
                                Player.STATE_ENDED -> playNextInPlaylist()
                                Player.STATE_IDLE -> isPrepared = false
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying = isPlaying
                        }
                    })
                }
            isInitialized = true
        }
    }

    fun rewind() {
        currentlyPlayingSong?.let { setSong(it) }
    }

    fun seekTo(pos: Long, rawPosition: Boolean = true) {
        exoPlayer?.let { player ->
            val positionMs = if (rawPosition) pos else (pos / 100) * player.duration.coerceAtLeast(1)
            player.seekTo(positionMs.coerceIn(0, player.duration))
            bus.post(Events.SongWas_UsedSeek())
        }
    }

    fun openPlaylist(playlist: AtomicReference<Playlist>) {
        currentPlaylist = playlist
        playlist.get().songsList?.let { songs ->
            if (songs.isNotEmpty()) {
                exoPlayer?.repeatMode = if (isLoopingInPlaylist)
                    Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            }
        }
    }

    fun playNextInPlaylist() {
        currentPlaylist?.get()?.let { playlist ->
            currentlyPlayingSong?.let { currentSong ->
                if (playlist.hasNextAfter(currentSong)) {
                    val nextIndex = playlist.songsList!!.indexOf(currentSong) + 1
                    setSong(playlist.songsList!![nextIndex])
                }
            }
        }
    }

    fun playPreviousInPlaylist() {
        currentPlaylist?.get()?.let { playlist ->
            currentlyPlayingSong?.let { currentSong ->
                if (playlist.hasPreviousBefore(currentSong)) {
                    val prevIndex = playlist.songsList!!.indexOf(currentSong) - 1
                    setSong(playlist.songsList!![prevIndex])
                }
            }
        }
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        exoPlayer?.volume = (leftVolume + rightVolume) / 2f
    }

    fun getAudioSessionID(): Int {
        return exoPlayer?.audioSessionId ?: 0
    }

    fun setSong(song: Song, playlist: AtomicReference<Playlist>? = null) {
        initializePlayer(Application.instance)


        val lastSong = currentlyPlayingSong
        exoPlayer?.let { player ->
            try {

                currentlyPlayingSong = song.from(SongsGlobalVars.allSongs)

                val mediaItem = MediaItem.Builder()
                    .setUri(song.songUri.toUri())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.author)
                            .build()
                    )
                    .setTag(song) // Store original Song object
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()

                // Update song stats
                song.from(SongsGlobalVars.allSongs)?.apply {
                    timesListened++
                    lastPlayed = LocalDateTime.now().toString()
                }

                SongsGlobalVars.RecentlyPlayed.apply {
                    songsList?.remove(song.from(SongsGlobalVars.allSongs))
                    Log.i("WTF",song.from(SongsGlobalVars.allSongs)!!.toString())
                    add(song.from(SongsGlobalVars.allSongs)!!)
                }

                playlist?.let {
                    openPlaylist(it)
                    //exoPlayer!!.setMediaItems(currentPlaylist!!.get().songsList!!.map { p->p.toMediaItem() },it.get().songsList!!.indexOf(currentlyPlayingSong),0)
                }

                bus.post(Events.SongWasChanged(lastSong, currentlyPlayingSong))


                myExoPlayer.start()



            } catch (ex: Exception) {
                Log.e(Logs.MEDIA_SOUND.toString(), "Error setting song", ex)
                    lastSong?.let { setSong(it) }
            }
        }
    }

    fun start() {
        exoPlayer?.let { player ->
            if (currentlyPlayingSong != null && !player.isPlaying) {
                player.play()
                _isPlaying = true
                bus.post(Events.SongWasStarted())
            }
        }
    }

    fun stop() {
        exoPlayer?.let { player ->
            if (currentlyPlayingSong != null) {
                player.stop()
                currentlyPlayingSong = null
                _isPlaying = false
                bus.post(Events.SongWasStopped())
            }
        }
    }

    fun toggle() {
        exoPlayer?.let { player ->
            if (currentlyPlayingSong != null) {
                if (player.isPlaying) pause() else start()
            }
        }
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun reset() {
        exoPlayer?.let { player ->
            if (currentlyPlayingSong != null) {
                player.stop()
                player.clearMediaItems()
                isPrepared = false
                _isPlaying = false
                bus.post(Events.SongWasReset())
            }
        }
    }

    fun release() {
        exoPlayer?.let { player ->
            player.release()
            exoPlayer = null
            isPrepared = false
            isInitialized = false
            _isPlaying = false
            currentPlaylist = null
            currentlyPlayingSong = null
        }
    }

    fun pause() {
        exoPlayer?.let { player ->
            if (currentlyPlayingSong != null && player.isPlaying) {
                player.pause()
                _isPlaying = false
                bus.post(Events.SongWasPaused())
            }
        }
    }
}