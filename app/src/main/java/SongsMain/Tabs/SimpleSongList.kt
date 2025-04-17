package SongsMain.Tabs

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.example.composepls.R
import java.nio.file.Path
import androidx.core.net.toUri

class SimpleSongList : Fragment(R.layout.fragment_simple_song_list) {
    lateinit var mediaplayer:MediaPlayer

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)









        val audioView = requireView().findViewById(R.id.songView) as ListView

        val audioList = ArrayList<Uri>()

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        ) // Can include more data for more details and check it.

        val audioCursor: Cursor? = requireActivity().getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            "${MediaStore.Audio.Media.DISPLAY_NAME} like ?",
            arrayOf("%.MP3"),
            null
        )

        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                do {
                    val id = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val relativepath = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH))
                    val url:String = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.Media.DATA))



                    audioList.add(url.toUri())
                } while (audioCursor.moveToNext())
            }
        }
        audioCursor!!.close()

        val listToString = audioList.map { it.path }

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, listToString)
        audioView.adapter = adapter









        val butonplaystop = requireView().findViewById<Button>(R.id.button7)

        if(audioList.isNotEmpty()){

            val first = listToString.first().toString()

            Log.i("TESTS",first.toString())



            butonplaystop.setOnClickListener {
                fun initializemedia() {
                    mediaplayer = MediaPlayer().apply {
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()


                        setDataSource(first) // using path
                        prepare()
                    }
                }

                initializemedia()
                if(mediaplayer.isPlaying){
                    mediaplayer.stop()

                }else{
                    initializemedia()

                        mediaplayer.start()


                }
            }
        }




    }


}