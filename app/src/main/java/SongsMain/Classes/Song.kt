package SongsMain.Classes

import SongsMain.Variables.SongsGlobalVars
import java.time.LocalDateTime
import java.time.ZoneId

class Song(
 var songUri: String,
 var title: String,
 var thumbnail: String? = "",
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

      fun ArrayList<Song>?.takeYourPartFromGlobal(from: ArrayList<Song> = SongsGlobalVars.allSongs){

          this?.forEachIndexed {index, song->
              from.find { it->
                  it==song
              }.apply {
                  if(this!=null)
                    this@takeYourPartFromGlobal[index]=this.from(from)!!
              }
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
     *
     *
     * */
   fun compareAndCompleteTheFirst(resultInto: ArrayList<Song>, val2: ArrayList<Song>) {




    resultInto.forEach {
        if (val2.contains(it)) {
            val2.indexOf(it).apply {
                if (this >= 0) {
                    // adica exista in val2 SI este pe o pozitie pe care o am ca "this"

                    it.isHidden = val2[this].isHidden
                    it.isHidden = val2[this].isHidden
                    it.timesListened = val2[this].timesListened
                    it.lastPlayed = val2[this].lastPlayed
                    it.title=val2[this].title
                }
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
