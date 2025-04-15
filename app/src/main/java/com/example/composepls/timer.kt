package com.example.composepls

import android.database.Cursor
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import java.time.LocalDateTime


class timer : Fragment(R.layout.fragment_timer) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val audioView = requireView().findViewById(R.id.songView) as ListView

        val audioList = ArrayList<String>()

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
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
                    val audioIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)



                    audioList.add(audioCursor.getString(audioIndex))
                } while (audioCursor.moveToNext())
            }
        }
        audioCursor!!.close()

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, audioList)
        audioView.adapter = adapter

    }
}