package SongsMain.Classes

import android.graphics.Bitmap
import androidx.annotation.Nullable
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration

 class Song(var songUri:String,var thumbnail:Bitmap?=null,var title: String,var author:String,var duration: Long){
  var dateAdded: LocalDateTime=LocalDateTime.now()
  var timesListened:Int=0
  var lastPlayed:LocalDateTime? = null



}
