package SongsMain.aaTest

/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import SongsMain.Service.Media3Service.Companion.NOTIFICATION_ID
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Variables.SongsGlobalVars.CHANNEL_ID
import SongsMain.Variables.SongsGlobalVars.CHANNEL_NAME
import android.content.res.AssetManager
import android.net.Uri
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MediaMetadata
import java.io.BufferedReader
import java.lang.StringBuilder
import org.json.JSONObject
import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import com.example.composepls.R

/** A [MediaLibraryService.MediaLibrarySession.Callback] implementation. */
open class DemoMediaLibrarySessionCallback(context: Context) :
    MediaLibraryService.MediaLibrarySession.Callback {

    init {
        MediaItemTree.initialize(context.assets)
    }

    private val commandButtons: List<CommandButton> =
        listOf(
            CommandButton.Builder(CommandButton.ICON_SHUFFLE_OFF)
                .setDisplayName("displayname")
                .setSessionCommand(SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY))
                .build(),
            CommandButton.Builder(CommandButton.ICON_SHUFFLE_ON)
                .setDisplayName("displayname2")
                .setSessionCommand(SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY))
                .build(),
        )

    @OptIn(UnstableApi::class) // MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
    val mediaNotificationSessionCommands =
        MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            .also { builder ->
                // Put all custom session commands in the list that may be used by the notification.
                commandButtons.forEach { commandButton ->
                    commandButton.sessionCommand?.let { builder.add(it) }
                }
            }
            .build()

    // ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
    // ConnectionResult.AcceptedResultBuilder
    @OptIn(UnstableApi::class)
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult {
        if (
            session.isMediaNotificationController(controller) ||
            session.isAutomotiveController(controller) ||
            session.isAutoCompanionController(controller)
        ) {
            // Select the button to display.
            val customButton = commandButtons[if (session.player.shuffleModeEnabled) 1 else 0]
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(mediaNotificationSessionCommands)
                .setMediaButtonPreferences(ImmutableList.of(customButton))
                .build()
        }
        // Default commands without media button preferences for common controllers.
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
    }

    @OptIn(UnstableApi::class) // MediaSession.isMediaNotificationController
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON == customCommand.customAction) {
            // Enable shuffling.
            session.player.shuffleModeEnabled = true
            // Change the media button preferences to contain the `Disable shuffling` button.
            session.setMediaButtonPreferences(
                session.mediaNotificationControllerInfo!!,
                ImmutableList.of(commandButtons[1]),
            )
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        } else if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF == customCommand.customAction) {
            // Disable shuffling.
            session.player.shuffleModeEnabled = false
            // Change the media button preferences to contain the `Enable shuffling` button.
            session.setMediaButtonPreferences(
                session.mediaNotificationControllerInfo!!,
                ImmutableList.of(commandButtons[0]),
            )
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
        return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
    }

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(LibraryResult.ofItem(MediaItemTree.getRootItem(), params))
    }

    @OptIn(UnstableApi::class) // SessionError.ERROR_BAD_VALUE
    override fun onGetItem(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        MediaItemTree.getItem(mediaId)?.let {
            return Futures.immediateFuture(LibraryResult.ofItem(it, /* params= */ null))
        }
        return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
    }

    @OptIn(UnstableApi::class) // SessionError.ERROR_BAD_VALUE
    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val children = MediaItemTree.getChildren(parentId)
        if (children.isNotEmpty()) {
            return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
        }
        return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<List<MediaItem>> {
        return Futures.immediateFuture(resolveMediaItems(mediaItems))
    }

    @OptIn(UnstableApi::class) // MediaSession.MediaItemsWithStartPosition
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        browser: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaItemsWithStartPosition> {
        if (mediaItems.size == 1) {
            // Try to expand a single item to a playlist.
            maybeExpandSingleItemToPlaylist(mediaItems.first(), startIndex, startPositionMs)?.also {
                return Futures.immediateFuture(it)
            }
        }
        return Futures.immediateFuture(
            MediaItemsWithStartPosition(resolveMediaItems(mediaItems), startIndex, startPositionMs)
        )
    }

    private fun resolveMediaItems(mediaItems: List<MediaItem>): List<MediaItem> {
        val playlist = mutableListOf<MediaItem>()
        mediaItems.forEach { mediaItem ->
            if (mediaItem.mediaId.isNotEmpty()) {
                MediaItemTree.expandItem(mediaItem)?.let { playlist.add(it) }
            } else if (mediaItem.requestMetadata.searchQuery != null) {
                playlist.addAll(MediaItemTree.search(mediaItem.requestMetadata.searchQuery!!))
            }
        }
        return playlist
    }

    @OptIn(UnstableApi::class) // MediaSession.MediaItemsWithStartPosition
    private fun maybeExpandSingleItemToPlaylist(
        mediaItem: MediaItem,
        startIndex: Int,
        startPositionMs: Long,
    ): MediaItemsWithStartPosition? {
        var playlist = listOf<MediaItem>()
        var indexInPlaylist = startIndex
        MediaItemTree.getItem(mediaItem.mediaId)?.apply {
            if (mediaMetadata.isBrowsable == true) {
                // Get children browsable item.
                playlist = MediaItemTree.getChildren(mediaId)
            } else if (requestMetadata.searchQuery == null) {
                // Try to get the parent and its children.
                MediaItemTree.getParentId(mediaId)?.let {
                    playlist =
                        MediaItemTree.getChildren(it).map { mediaItem ->
                            if (mediaItem.mediaId == mediaId) MediaItemTree.expandItem(mediaItem)!! else mediaItem
                        }
                    indexInPlaylist = MediaItemTree.getIndexInMediaItems(mediaId, playlist)
                }
            }
        }
        if (playlist.isNotEmpty()) {
            return MediaItemsWithStartPosition(playlist, indexInPlaylist, startPositionMs)
        }
        return null
    }

    override fun onSearch(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        session.notifySearchResultChanged(browser, query, MediaItemTree.search(query).size, params)
        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return Futures.immediateFuture(LibraryResult.ofItemList(MediaItemTree.search(query), params))
    }

    companion object {
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON =
            "android.media3.session.demo.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF =
            "android.media3.session.demo.SHUFFLE_OFF"
    }
}






































open class DemoPlaybackService : MediaLibraryService() {

    private lateinit var mediaLibrarySession: MediaLibrarySession

    companion object {


    }

    /**
     * Returns the single top session activity. It is used by the notification when the app task is
     * active and an activity is in the fore or background.
     *
     * Tapping the notification then typically should trigger a single top activity. This way, the
     * user navigates to the previous activity when pressing back.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getSingleTopActivity(): PendingIntent? = null

    /**
     * Returns a back stacked session activity that is used by the notification when the service is
     * running standalone as a foreground service. This is typically the case after the app has been
     * dismissed from the recent tasks, or after automatic playback resumption.
     *
     * Typically, a playback activity should be started with a stack of activities underneath. This
     * way, when pressing back, the user doesn't land on the home screen of the device, but on an
     * activity defined in the back stack.
     *
     * See [androidx.core.app.TaskStackBuilder] to construct a back stack.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getBackStackedActivity(): PendingIntent? = null

    /**
     * Creates the library session callback to implement the domain logic. Can be overridden to return
     * an alternative callback, for example a subclass of [DemoMediaLibrarySessionCallback].
     *
     * This method is called when the session is built by the [DemoPlaybackService].
     */
    protected open fun createLibrarySessionCallback(): MediaLibrarySession.Callback {
        return DemoMediaLibrarySessionCallback(this)
    }

    @OptIn(UnstableApi::class) // MediaSessionService.setListener
    override fun onCreate() {
        super.onCreate()
        initializeSessionAndPlayer()
        setListener(MediaSessionServiceListener())
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    // MediaSession.setSessionActivity
    // MediaSessionService.clearListener
    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        getBackStackedActivity()?.let { mediaLibrarySession.setSessionActivity(it) }
        mediaLibrarySession.release()
        mediaLibrarySession.player.release()
        clearListener()
        super.onDestroy()
    }

    private fun initializeSessionAndPlayer() {
        val player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
                .build()
        player.addAnalyticsListener(EventLogger())

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, createLibrarySessionCallback())
                .also { builder -> getSingleTopActivity()?.let { builder.setSessionActivity(it) } }
                .build()
                .also { mediaLibrarySession ->
                    // The media session always supports skip, except at the start and end of the playlist.
                    // Reserve the space for the skip action in these cases to avoid custom actions jumping
                    // around when the user skips.
                    mediaLibrarySession.setSessionExtras(
                        bundleOf(
                            MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_PREV to true,
                            MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_NEXT to true,
                        )
                    )
                }
    }

    @OptIn(UnstableApi::class) // MediaSessionService.Listener
    private inner class MediaSessionServiceListener : Listener {

        /**
         * This method is only required to be implemented on Android 12 or above when an attempt is made
         * by a media controller to resume playback when the {@link MediaSessionService} is in the
         * background.
         */
        override fun onForegroundServiceStartNotAllowedException() {
            if (
                Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Notification permission is required but not granted
                return
            }
            val notificationManagerCompat = NotificationManagerCompat.from(this@DemoPlaybackService)
            ensureNotificationChannel(notificationManagerCompat)
            val builder =
                NotificationCompat.Builder(this@DemoPlaybackService, CHANNEL_ID)
                    .setSmallIcon(R.drawable.plus_default)
                    .setContentTitle("contenttitle")
                    .setStyle(
                        NotificationCompat.BigTextStyle()//.bigText(getString(R.string.notification_content_text))
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .also { builder -> getBackStackedActivity()?.let { builder.setContentIntent(it) } }
            startForeground(NOTIFICATION_ID,builder.build())
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())

        }
    }

    private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        if (
            notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null
        ) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        notificationManagerCompat.createNotificationChannel(channel)
    }
}





































/**
 * A sample media catalog that represents media items as a tree.
 *
 * It fetched the data from {@code catalog.json}. The root's children are folders containing media
 * items from the same album/artist/genre.
 *
 * Each app should have their own way of representing the tree. MediaItemTree is used for
 * demonstration purpose only.
 */
object MediaItemTree {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var titleMap: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var isInitialized = false
    private const val ROOT_ID = "[rootID]"
    private const val ALBUM_ID = "[albumID]"
    private const val GENRE_ID = "[genreID]"
    private const val ARTIST_ID = "[artistID]"
    private const val ALBUM_PREFIX = "[album]"
    private const val GENRE_PREFIX = "[genre]"
    private const val ARTIST_PREFIX = "[artist]"
    private const val ITEM_PREFIX = "[item]"

    private class MediaItemNode(val item: MediaItem) {
        val searchTitle = normalizeSearchText(item.mediaMetadata.title)
        val searchText =
            StringBuilder()
                .append(searchTitle)
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.subtitle))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.artist))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.albumArtist))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.albumTitle))
                .toString()

        private val children: MutableList<MediaItem> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(treeNodes[childID]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        mediaType: @MediaMetadata.MediaType Int,
        subtitleConfigurations: List<SubtitleConfiguration> = mutableListOf(),
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null
    ): MediaItem {
        val metadata =
            MediaMetadata.Builder()
                .setAlbumTitle(album)
                .setTitle(title)
                .setArtist(artist)
                .setGenre(genre)
                .setIsBrowsable(isBrowsable)
                .setIsPlayable(isPlayable)
                .setArtworkUri(imageUri)
                .setMediaType(mediaType)
                .build()

        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setSubtitleConfigurations(subtitleConfigurations)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
    }

    private fun loadJSONFromAsset(assets: AssetManager): String =
        assets.open("catalog.json").bufferedReader().use(BufferedReader::readText)

    fun initialize(assets: AssetManager) {
        if (isInitialized) return
        isInitialized = true
        // create root and folders for album/artist/genre.
        treeNodes[ROOT_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Root Folder",
                    mediaId = ROOT_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
                )
            )
        treeNodes[ALBUM_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Album Folder",
                    mediaId = ALBUM_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS
                )
            )
        treeNodes[ARTIST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Artist Folder",
                    mediaId = ARTIST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS
                )
            )
        treeNodes[GENRE_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Genre Folder",
                    mediaId = GENRE_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_GENRES
                )
            )
        treeNodes[ROOT_ID]!!.addChild(ALBUM_ID)
        treeNodes[ROOT_ID]!!.addChild(ARTIST_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)

        // Here, parse the json file in asset for media list.
        // We use a file in asset for demo purpose
        val jsonObject = JSONObject(loadJSONFromAsset(assets))
        val mediaList = jsonObject.getJSONArray("media")

        // create subfolder with same artist, album, etc.
        for (i in 0 until mediaList.length()) {
            addNodeToTree(mediaList.getJSONObject(i))
        }
    }

    private fun addNodeToTree(mediaObject: JSONObject) {

        val id = mediaObject.getString("id")
        val album = mediaObject.getString("album")
        val title = mediaObject.getString("title")
        val artist = mediaObject.getString("artist")
        val genre = mediaObject.getString("genre")
        val subtitleConfigurations: MutableList<SubtitleConfiguration> = mutableListOf()
        if (mediaObject.has("subtitles")) {
            val subtitlesJson = mediaObject.getJSONArray("subtitles")
            for (i in 0 until subtitlesJson.length()) {
                val subtitleObject = subtitlesJson.getJSONObject(i)
                subtitleConfigurations.add(
                    SubtitleConfiguration.Builder(Uri.parse(subtitleObject.getString("subtitle_uri")))
                        .setMimeType(subtitleObject.getString("subtitle_mime_type"))
                        .setLanguage(subtitleObject.getString("subtitle_lang"))
                        .build()
                )
            }
        }
        val sourceUri = Uri.parse(mediaObject.getString("source"))
        val imageUri = Uri.parse(mediaObject.getString("image"))
        // key of such items in tree
        val idInTree = ITEM_PREFIX + id
        val albumFolderIdInTree = ALBUM_PREFIX + album
        val artistFolderIdInTree = ARTIST_PREFIX + artist
        val genreFolderIdInTree = GENRE_PREFIX + genre

        treeNodes[idInTree] =
            MediaItemNode(
                buildMediaItem(
                    title = title,
                    mediaId = idInTree,
                    isPlayable = true,
                    isBrowsable = false,
                    mediaType = MediaMetadata.MEDIA_TYPE_MUSIC,
                    subtitleConfigurations,
                    album = album,
                    artist = artist,
                    genre = genre,
                    sourceUri = sourceUri,
                    imageUri = imageUri
                )
            )

        titleMap[title.lowercase()] = treeNodes[idInTree]!!

        if (!treeNodes.containsKey(albumFolderIdInTree)) {
            treeNodes[albumFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = album,
                        mediaId = albumFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
                        subtitleConfigurations,
                        album = null,
                        artist = null,
                        genre = null,
                        sourceUri = null,
                        imageUri
                    )
                )
            treeNodes[ALBUM_ID]!!.addChild(albumFolderIdInTree)
        }
        treeNodes[albumFolderIdInTree]!!.addChild(idInTree)

        // add into artist folder
        if (!treeNodes.containsKey(artistFolderIdInTree)) {
            treeNodes[artistFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = artist,
                        mediaId = artistFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
                        subtitleConfigurations
                    )
                )
            treeNodes[ARTIST_ID]!!.addChild(artistFolderIdInTree)
        }
        treeNodes[artistFolderIdInTree]!!.addChild(idInTree)

        // add into genre folder
        if (!treeNodes.containsKey(genreFolderIdInTree)) {
            treeNodes[genreFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = genre,
                        mediaId = genreFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
                        subtitleConfigurations
                    )
                )
            treeNodes[GENRE_ID]!!.addChild(genreFolderIdInTree)
        }
        treeNodes[genreFolderIdInTree]!!.addChild(idInTree)
    }

    fun getItem(id: String): MediaItem? {
        return treeNodes[id]?.item
    }

    fun expandItem(item: MediaItem): MediaItem? {
        val treeItem = getItem(item.mediaId) ?: return null
        @OptIn(UnstableApi::class) // MediaMetadata.populate
        val metadata = treeItem.mediaMetadata.buildUpon().populate(item.mediaMetadata).build()
        return item
            .buildUpon()
            .setMediaMetadata(metadata)
            .setSubtitleConfigurations(treeItem.localConfiguration?.subtitleConfigurations ?: listOf())
            .setUri(treeItem.localConfiguration?.uri)
            .build()
    }

    /**
     * Returns the media ID of the parent of the given media ID, or null if the media ID wasn't found.
     *
     * @param mediaId The media ID of which to search the parent.
     * @Param parentId The media ID of the media item to start the search from, or undefined to search
     *   from the top most node.
     */
    fun getParentId(mediaId: String, parentId: String = ROOT_ID): String? {
        for (child in treeNodes[parentId]!!.getChildren()) {
            if (child.mediaId == mediaId) {
                return parentId
            } else if (child.mediaMetadata.isBrowsable == true) {
                val nextParentId = getParentId(mediaId, child.mediaId)
                if (nextParentId != null) {
                    return nextParentId
                }
            }
        }
        return null
    }

    /**
     * Returns the index of the [MediaItem] with the give media ID in the given list of items. If the
     * media ID wasn't found, 0 (zero) is returned.
     */
    fun getIndexInMediaItems(mediaId: String, mediaItems: List<MediaItem>): Int {
        for ((index, child) in mediaItems.withIndex()) {
            if (child.mediaId == mediaId) {
                return index
            }
        }
        return 0
    }

    /**
     * Tokenizes the query into a list of words with at least two letters and searches in the search
     * text of the [MediaItemNode].
     */
    fun search(query: String): List<MediaItem> {
        val matches: MutableList<MediaItem> = mutableListOf()
        val titleMatches: MutableList<MediaItem> = mutableListOf()
        val words = query.split(" ").map { it.trim().lowercase() }.filter { it.length > 1 }
        titleMap.keys.forEach { title ->
            val mediaItemNode = titleMap[title]!!
            for (word in words) {
                if (mediaItemNode.searchText.contains(word)) {
                    if (mediaItemNode.searchTitle.contains(query.lowercase())) {
                        titleMatches.add(mediaItemNode.item)
                    } else {
                        matches.add(mediaItemNode.item)
                    }
                    break
                }
            }
        }
        titleMatches.addAll(matches)
        return titleMatches
    }

    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    fun getChildren(id: String): List<MediaItem> {
        return treeNodes[id]?.getChildren() ?: listOf()
    }

    private fun normalizeSearchText(text: CharSequence?): String {
        if (text.isNullOrEmpty() || text.trim().length == 1) {
            return ""
        }
        return "$text".trim().lowercase()
    }
}