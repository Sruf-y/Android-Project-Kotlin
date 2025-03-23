package com.example.composepls

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlarmActivity : AppCompatActivity(),AccessibleExpandingFAB.OnActivationStateChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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
