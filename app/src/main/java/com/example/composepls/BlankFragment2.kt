package com.example.composepls

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class BlankFragment2 : Fragment(R.layout.fragment_blank2) {
    lateinit var mediaplayer:MediaPlayer
    private lateinit var playButton: Button
    lateinit var layWithButton:ConstraintLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val localContext=requireContext()
        playButton = requireView().findViewById(R.id.button6);
        layWithButton = requireView().findViewById<ConstraintLayout>(R.id.middlescreen);
        mediaplayer=MediaPlayer.create(requireContext(),R.raw.vineboom)









        playButton.setOnClickListener {
            playButton.isEnabled=false
            //daca nu este initializat sau inca difuza sunetul, reinitializeaza
            if (mediaplayer.isPlaying) {
                mediaplayer = MediaPlayer.create(localContext, R.raw.vineboom) // Initialize MediaPlayer with the audio file
            }


            mediaplayer.start();


            playButton.isEnabled=true
        }
    }

}
