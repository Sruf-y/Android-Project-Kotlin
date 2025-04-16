package com.example.composepls

import Functions.AskForPermissionsAtStart
import Functions.OpenAppSettings
import Functions.VerifyPermissions
import SongsMain.SongsMain_view
import StorageTest.StorageMainActivity
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class BlankFragment2 : Fragment(R.layout.fragment_blank2) {
    lateinit var mediaplayer:MediaPlayer
    private lateinit var playButton: Button
    lateinit var layWithButton:ConstraintLayout

    lateinit var storageButton:Button




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AskForPermissionsAtStart(requireContext() as Activity,GlobalValues.System.RequiredPermissions)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val localContext = requireContext()
        playButton = requireView().findViewById(R.id.button6);
        layWithButton = requireView().findViewById<ConstraintLayout>(R.id.middlescreen);
        mediaplayer = MediaPlayer.create(requireContext(), R.raw.vineboom)




        playButton.setOnClickListener {
            playButton.isEnabled = false
            //daca nu este initializat sau inca difuza sunetul, reinitializeaza
            if (mediaplayer.isPlaying) {
                mediaplayer = MediaPlayer.create(
                    localContext,
                    R.raw.vineboom
                ) // Initialize MediaPlayer with the audio file
            }

            mediaplayer.start();

            playButton.isEnabled = true
        }





        storageButton= requireView().findViewById<Button>(R.id.butonEnterFileExplorer)

        storageButton.setOnClickListener { it ->

            val permisions_are_ok = VerifyPermissions(requireContext() as Activity,GlobalValues.System.RequiredPermissions)

            if (permisions_are_ok) {
                val intent = Intent(requireContext(), StorageMainActivity::class.java)

                val path: String = Environment.getExternalStorageDirectory().path
                intent.putExtra("path", path)

                startActivity(intent)
            }
            else{
                OpenAppSettings(context)
            }
        }


        val musicButton = requireView().findViewById<Button>(R.id.button4)
        musicButton.setOnClickListener {
            val intent = Intent(requireContext(), SongsMain_view::class.java)
            startActivity(intent)
        }


    }






}
