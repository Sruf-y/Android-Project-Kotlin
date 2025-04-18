package com.example.composepls

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlarmActivity : AppCompatActivity(),AccessibleExpandingFAB.OnActivationStateChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }


        val textbox:TextView=findViewById(R.id.auxtextview)
        val fab = findViewById<AccessibleExpandingFAB>(R.id.floatingActionButton)
        fab.setOnActivationStateChangedListener(this)


    }

    override fun onActivationStateChanged(isActivated: Boolean) {

        //Log.i("MYTAG","$isActivated")
        if(isActivated) {
            findViewById<TextView>(R.id.auxtextview)?.text = "Canceled"

            Handler(Looper.getMainLooper()).postDelayed({

                //finish the activity
                finish()

            }, 600)
        }
    }
}
