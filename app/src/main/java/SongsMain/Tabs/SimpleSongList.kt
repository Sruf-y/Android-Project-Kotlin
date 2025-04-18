package SongsMain.Tabs

import Functions.OpenAppSettings
import Functions.VerifyPermissions
import GlobalValues.Media
import SongsMain.Classes.Song
import SongsMain.Classes.myMediaPlayer
import StorageTest.StorageMainActivity
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.composepls.R
import java.nio.file.Path
import androidx.core.net.toUri
import com.bumptech.glide.load.resource.file.FileDecoder
import kotlinx.coroutines.stream.consumeAsFlow
import okhttp3.Request
import java.io.FileDescriptor

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list) {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



       val  audioView = requireView().findViewById(R.id.songView) as ListView











        val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val lista = ArrayList<Song>()

        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_ADDED,
            MediaStore.Downloads.MIME_TYPE,
            MediaStore.Downloads.RELATIVE_PATH,   // Path relative to downloads directory
            MediaStore.Downloads.DURATION
        )

        val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"

        val selectionArgs= arrayOf("%")

        val cursor: Cursor? = requireActivity().contentResolver.query(
            FROM,
            projection,
            selection,
            selectionArgs,
            null
        )


        cursor?.use {
            // Automatically closes the cursor when done
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val relativePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH))
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Downloads.DURATION))
                // Create content URI for the file
                val contentUri = ContentUris.withAppendedId(FROM, id)

                lista.add(Song(contentUri.toString(),title,duration))



            }
        }



        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            lista.map{it.title}
        )

        audioView.adapter = adapter

        Log.i("TESTS","Nr melodii(SimpleSongList): "+lista.size.toString())







        val butonplaystop = requireView().findViewById<Button>(R.id.button7)
        val first = lista.firstOrNull { p->p.title.lowercase().contains("pick a side") }



        if(lista.isNotEmpty()){


            if(first!=null) {

                myMediaPlayer.setSong(requireActivity(),first.songUri)


                butonplaystop.setOnClickListener {


                        myMediaPlayer.toggle()

                }

                butonplaystop.setOnLongClickListener {
                    myMediaPlayer.stop()

                    true
                }
            }
        }




    }





    override fun onPause() {
        super.onPause()


    }

    override fun onResume() {
        super.onResume()


    }
}