package SongsMain.Classes

import Functions.fileExists
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.versionedparcelable.VersionedParcelize
import coil3.toCoilUri
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.AbstractQueue
import java.util.Deque
import java.util.PriorityQueue
import java.util.concurrent.SynchronousQueue

@Parcelize
class Song(
    var id:Long,
 var songUri: String,
 var title: String="",
 var thumbnail: String = "",
 var author: String="",
 var duration: Long,
 var dateAdded: Long = LocalDateTime.now()
  .atZone(ZoneId.systemDefault())  // Convert to ZonedDateTime
  .toInstant()                     // Convert to Instant
  .epochSecond,
    var albumName:String = "",
    var timesListened:Int=0,
    var lastPlayed:String? = null,
    var isHidden:Boolean = false,
    var isFavorite: Boolean = false) : Serializable, Parcelable {



//  constructor( id:Long,songUri:String, title: String, thumbnail:String="", author:String, duration: Long, isHidden:Boolean,isFavorite:Boolean):this(id,songUri,title,thumbnail,author,duration){
//   this.isHidden=isHidden
//   this.isFavorite=isFavorite
//
//  }


  companion object {
      // Add this to your Song class or as an extension

      val emptySong:Song=Song(
          id = -1,
          songUri = "",
          duration = -1
      )





      fun Song.toMediaItemCompat(): MediaBrowserCompat.MediaItem {
          val extras = Bundle().apply {
              putLong(METADATA_KEY_MEDIA_ID, id)
              putString(METADATA_KEY_TITLE, title)
              putString(METADATA_KEY_ARTIST, author)
              putString(METADATA_KEY_ALBUM_ART_URI, thumbnail)
              putLong(METADATA_KEY_DURATION, duration)
              putBoolean("isFavorite", isFavorite)
              putInt("timesListened", timesListened)
          }

          val description = MediaDescriptionCompat.Builder()
              .setMediaId(id.toString())
              .setTitle(title)
              .setSubtitle(author)
              .setIconUri(thumbnail.takeIf { it.isNotEmpty() }?.toUri())
              .setExtras(extras)
              .build()

          return MediaBrowserCompat.MediaItem(
              description,
              MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
          )
      }

      // Constants for metadata keys (you can define these at the top of your file)
      private const val METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID"
      private const val METADATA_KEY_TITLE = "android.media.metadata.TITLE"
      private const val METADATA_KEY_ARTIST = "android.media.metadata.ARTIST"
      private const val METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI"
      private const val METADATA_KEY_DURATION = "android.media.metadata.DURATION"



      fun Song.toMediaItem(): MediaItem {
          return MediaItem.Builder()
              .setMediaId(this.id.toString())  // Unique identifier
              .setUri(this.songUri)      // File path/URL
              .setMediaMetadata(
                  MediaMetadata.Builder()
                      .setTitle(this.title)
                      .setArtist(this.author)
                      .setArtworkUri(File(this.thumbnail).toUri())
                      .build()
              ).build()
      }

      fun Song.from(list: ArrayList<Song>): Song? {

          val aux = list.find { it == this }
          //Log.i("TESTS", "aux is $aux and has timeslistened as ${aux?.timesListened}")
          return aux // Returns the actual reference from the list
      }


      fun ArrayList<Song>?.takeYourPartFromGlobal(from: ArrayList<Song> = SongsGlobalVars.allSongs){

          val marked_for_deletion:ArrayList<Song> = ArrayList<Song>()

          this?.forEachIndexed {index, song-> // song from playlist


              if(!song.songUri.toUri().fileExists(Application.instance)){
                  Log.i("wtf","Song doesn't exist at takeYourPartFromGlobal: "+song.songUri)
                  marked_for_deletion.add(song)
                  return@forEachIndexed
              }

              from.find { it-> // it = song from allsongs
                  it==song
              }.apply {
                  if(this!=null)
                    this@takeYourPartFromGlobal[index]=this.from(from)!!
              }

          }

          marked_for_deletion.forEach { it->
              Log.i("WTF","A fost sters"+it.toString())
              this?.remove(it)
          }
      }







   fun timeToSecondsOf(localDateTime: LocalDateTime): Long {
    return localDateTime
     .atZone(ZoneId.systemDefault())  // Convert to ZonedDateTime
     .toInstant()                     // Convert to Instant
     .epochSecond
   }

    /**
     *
     *Completes the first song list with the atributes of the second one, which should usually be the outdated one
     *
     * */
   fun compareAndCompleteTheFirst(resultInto: ArrayList<Song>, val2: ArrayList<Song>) {


    resultInto.forEach {
        if (val2.contains(it)) {
            val2.indexOf(it).apply {
                // adica exista in val2 SI este pe o pozitie pe care o am ca "this"

                it.isHidden = val2[this].isHidden
                it.timesListened = val2[this].timesListened
                it.lastPlayed = val2[this].lastPlayed
                it.title = val2[this].title
                it.thumbnail = val2[this].thumbnail
                it.isFavorite = val2[this].isFavorite
            }
        }
    }
   }
  }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song


        if (songUri != other.songUri) return false
        if(id != other.id) return false
//   if (thumbnail != null && other.thumbnail != null) {
//    if (thumbnail != other.thumbnail) return false
//   }


        return true
    }

    override fun hashCode(): Int {
        var result = 31 * songUri.hashCode()
        result = result + 31 * id.hashCode()
        return result
    }




}
