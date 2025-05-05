package SongsMain.aaTest



import Functions.setAnimationForKeyboard
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import com.example.composepls.R

class MainActivity : AppCompatActivity() {



    // bottom of the window
    var bottom:Int =0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        val root = findViewById<View>(R.id.root)
        val button = findViewById<View>(R.id.button)
        val edittext=findViewById<View>(R.id.editText)
        val main=findViewById<ConstraintLayout>(R.id.main)

        // layout:
        //
        // ConstraintLayout (root)
        //       |
        // ConstraintLayout (main)
        //     /    \
        // Button, EditText






        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

            // get the desired bototm of your window
            bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.setPadding(systemBars.left,systemBars.top,systemBars.right,systemBars.bottom)
            insets
        }



        setAnimationForKeyboard(view = main, initialTranslationY = main.translationY)
    }



}