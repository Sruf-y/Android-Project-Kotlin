package SongsMain.Classes

import SongsMain.SongMain_Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.net.toUri
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration

class Song(
 var songUri: String,
 var title: String,
 var thumbnail: String = "",
 var author: String,
 var duration: Long,
 var dateAdded: Long = LocalDateTime.now()
  .atZone(ZoneId.systemDefault())  // Convert to ZonedDateTime
  .toInstant()                     // Convert to Instant
  .epochSecond
) : java.io.Serializable {

  var timesListened:Int=0
  var lastPlayed:String? = null
  var isHidden:Boolean = false
  var isFavorite: Boolean = false



  constructor( songUri:String, title: String, thumbnail:String="", author:String, duration: Long, isHidden:Boolean,isFavorite:Boolean):this(songUri,title,thumbnail,author,duration){
   this.isHidden=isHidden
   this.isFavorite=isFavorite

  }

  companion object {

      fun Song.from(list: ArrayList<Song>): Song? {

          val aux = list.find { it == this }
          //Log.i("TESTS", "aux is $aux and has timeslistened as ${aux?.timesListened}")
          return aux // Returns the actual reference from the list
      }



      fun quickSaveGlobalList(context:Context){

          Functions.saveAsJson(
              context,
              "GlobalSongs",
              SongsGlobalVars.allSongs
          )
      }



   fun timeToSecondsOf(localDateTime: LocalDateTime): Long {
    return localDateTime
     .atZone(ZoneId.systemDefault())  // Convert to ZonedDateTime
     .toInstant()                     // Convert to Instant
     .epochSecond
   }


   fun compareAndCompleteTheFirst(resultInto: ArrayList<Song>, val2: ArrayList<Song>) {

    //updating
    resultInto.forEach {
     val2.indexOf(it).apply {
      if (this > -1) {
       // adica exista in val2 SI este pe o pozitie pe care o am ca "this"

       it.isHidden = val2[this].isHidden
       it.isHidden = val2[this].isHidden
       it.timesListened=val2[this].timesListened
       it.lastPlayed=val2[this].lastPlayed
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
//   if (thumbnail != null && other.thumbnail != null) {
//    if (thumbnail != other.thumbnail) return false
//   }


   return true
  }

  override fun hashCode(): Int {
   var result = duration.hashCode()
   result = 31 * result + songUri.hashCode()
   return result
  }


 }
