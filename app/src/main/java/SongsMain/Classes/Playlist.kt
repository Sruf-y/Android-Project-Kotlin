package SongsMain.Classes

import DataClasses_Ojects.Logs
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.Log
import com.example.composepls.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime


@Parcelize
data class Playlist(var title: String,var songsList: ArrayList<Song>?=null,var isUserOrdered:Boolean=true,var thumbnail: Int?=null):
    Parcelable {



    fun add(song:Song){
        try {
            if (songsList == null) {
                songsList = ArrayList<Song>()
            }
            song.dateAdded = Song.timeToSecondsOf(LocalDateTime.now())
            songsList?.add(song)
        }catch (ex: Exception){
            Log.e(Logs.LOGIC.toString(),ex.message.toString())
            ex.printStackTrace()
        }

    }

    fun remove(song:Song){
        try {
            songsList?.let {
                if (it.contains(song)) {
                    it.remove(song)
                }
            }
        }catch (ex: Exception){
            Log.e(Logs.LOGIC.toString(),ex.message.toString())
            ex.printStackTrace()
        }
    }

    fun hasNextAfter(song: Song): Boolean {
        try {
            if (songsList!!.contains(song)) {
                Log.i("TESTS", "This playlist contains this song.")
                if (songsList!!.indexOf(song) < songsList!!.size - 1) {
                    return true
                }
                Log.i("TESTS", "This song's index is the last song in the playlist .")
                return false
            }
            Log.i("TESTS", "This is not in the playlist .")
            return false
        } catch (ex: Exception) {
            Log.e(Logs.LOGIC.toString(), ex.message.toString())
            ex.printStackTrace()
            return false
        }
        return false
    }

    fun hasPreviousBefore(song: Song): Boolean {
        try {
            if (songsList!!.contains(song)) {
                if (songsList!!.indexOf(song) > 0) {
                    return true
                }
                return false
            }
            return false
        } catch (ex: Exception) {
            Log.e(Logs.LOGIC.toString(), ex.message.toString())
            ex.printStackTrace()
        } as Boolean
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Playlist

        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }


}