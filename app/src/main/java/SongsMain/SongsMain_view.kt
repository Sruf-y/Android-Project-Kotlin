package SongsMain

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.composepls.R

class SongsMain_view : AppCompatActivity() {

    lateinit var fragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_songs_main_view)
        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))


        enableEdgeToEdge(myStatusBarStyle,myNavigationBarStyle)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.navView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()+ WindowInsetsCompat.Type.navigationBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fragmentContainer=findViewById(R.id.fragmentContainerView)

        makeCurrentFragment(fragmentContainer,SongsMain.SongsMain_Base())










    }

    private fun makeCurrentFragment(container:FragmentContainerView,fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(container.id,fragment)
            commit();
        }
    }


}