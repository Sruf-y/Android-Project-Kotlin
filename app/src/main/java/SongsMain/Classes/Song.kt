package SongsMain.Classes

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.net.toUri
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration

 class Song(var songUri:String,var title: String,var thumbnail:File?=null,var author:String,var duration: Long):java.io.Serializable{
  var dateAdded: LocalDateTime=LocalDateTime.now()
  var timesListened:Int=0
  var lastPlayed:LocalDateTime? = null
  var bitmapofThumbNail:Bitmap?=null // For runtime usage only, json cannot save bitmaps apparently...







  override fun equals(other: Any?): Boolean {
   if (this === other) return true
   if (javaClass != other?.javaClass) return false

   other as Song

   if (duration != other.duration) return false
   if (songUri != other.songUri) return false
   if (title != other.title) return false
   if (author != other.author) return false
   if(thumbnail!=other.thumbnail) return false



   return true
  }

  override fun hashCode(): Int {
   var result = duration.hashCode()
   result = 31 * result + songUri.hashCode()
   result = 31 * result + title.hashCode()
   result = 31 * result + author.hashCode()
   return result
  }


 }
