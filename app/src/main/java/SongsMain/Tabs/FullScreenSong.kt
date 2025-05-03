package SongsMain.Tabs

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.composepls.R

class FullScreenSong : AppCompatActivity() {

    lateinit var main: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))

        enableEdgeToEdge(myStatusBarStyle, myNavigationBarStyle)
        setContentView(R.layout.activity_full_screen_song)

        main=findViewById(R.id.main)

        Functions.setInsetsforItems(mutableListOf(main))
    }
}