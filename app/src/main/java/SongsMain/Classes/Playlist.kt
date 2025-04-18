package SongsMain.Classes

import android.graphics.drawable.Drawable

data class Playlist(var thumbnail: Drawable,var title: String,var description:String,var songsList: ArrayList<Song>) {

}