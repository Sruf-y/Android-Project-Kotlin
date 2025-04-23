package SongsMain.Classes

import DataClasses_Ojects.Logs
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime

data class Playlist(var title: String,var songsList: ArrayList<Song>?=null,var isUserOrdered:Boolean=true) {


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
        return try {
            if (songsList?.contains(song) == true) {
                if (songsList!!.indexOf(song) < songsList!!.size)
                    true
            }
            false
        } catch (ex: Exception) {
            Log.e(Logs.LOGIC.toString(), ex.message.toString())
            ex.printStackTrace()
            false
        }
        false
    }

    fun hasPreviousBefore(song: Song): Boolean {
        return try {
            if (songsList?.contains(song) == true) {
                if (songsList!!.indexOf(song) > 0)
                    true
            }
            false
        } catch (ex: Exception) {
            Log.e(Logs.LOGIC.toString(), ex.message.toString())
            ex.printStackTrace()
        } as Boolean
        false
    }

}