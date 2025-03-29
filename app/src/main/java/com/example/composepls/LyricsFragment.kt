package com.example.composepls

import Utilities.Utils.Companion.dP
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceControl
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.lifecycle.findViewTreeViewModelStoreOwner

class LyricsFragment : Fragment(R.layout.fragment_lyrics) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val context= requireContext()
        val view = requireView()
        val workspace:ComposeView=view.findViewById(R.id.ComposeView1)

        workspace.setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color= Color(ContextCompat.getColor(context,R.color.backgrounds))
            ) {
                Column {

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            println("Button clicked")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(ContextCompat.getColor(context, R.color.green)),
                            contentColor = Color.White // Text color
                        ),
                    ) {
                        Text("Text nou")

                    }

                    Text(
                        text="Hello world!",
                        color = Color(ContextCompat.getColor(context,R.color.invertedBackgrounds)),

                        style = TextStyle(

                            textDecoration = TextDecoration.Underline,
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold

                        )

                    )
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