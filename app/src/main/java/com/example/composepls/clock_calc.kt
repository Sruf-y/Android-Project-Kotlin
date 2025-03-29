package com.example.composepls

import DataClasses_Ojects.Alarma_Item
import GlobalValues.sharedString
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.time.LocalDateTime


class clock_calc : Fragment(R.layout.fragment_clock_calc) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buton: Button = requireView().findViewById(R.id.button2)

        buton.setOnClickListener{
            val txt: TextView =requireView().findViewById(R.id.lista_de_alarme)
            txt.text= sharedString
        }
    }
}