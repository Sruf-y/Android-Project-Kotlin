package SongsMain

import DataClasses_Ojects.Logs
import Functions.AskForPermissionsAtStart
import SongsMain.Classes.myMediaPlayer
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.example.composepls.R
import com.google.android.material.navigation.NavigationView
import de.greenrobot.event.EventBus
import java.time.LocalTime


class OpenDrawerEvent()




class SongMain_Activity : AppCompatActivity() {

    val bus:EventBus = EventBus.getDefault();

    lateinit var fragmentContainer: FragmentContainerView
    lateinit var drawer: DrawerLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setContentView(R.layout.activity_songs_main_activity)





        fragmentContainer=findViewById(R.id.fragmentContainerView)


        if (savedInstanceState == null) {
            Log.i("TESTS","Activity set the initial fragment! +${LocalTime.now()}")


            makeCurrentFragment(fragmentContainer, SongsMain.SongsMain_Base::class.java)
        }













            val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
            val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))
            drawer=findViewById(R.id.drawerLayout)

            enableEdgeToEdge(myStatusBarStyle,myNavigationBarStyle)

            val navView: NavigationView= requireViewById(R.id.navView)





            if(resources.configuration.orientation== Configuration.ORIENTATION_LANDSCAPE) {
                navView.fitsSystemWindows=true
                fragmentContainer.fitsSystemWindows=true
            }
            else{
                navView.fitsSystemWindows=false
                fragmentContainer.fitsSystemWindows=false
            }


            ViewCompat.setOnApplyWindowInsetsListener(navView) { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.navigationBars() + WindowInsetsCompat.Type.statusBars())

                v.updatePaddingRelative(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }

            val mainCoord: CoordinatorLayout = findViewById(R.id.mainCoord)





















        bus.register(this)

        myMediaPlayer.initializeMediaPlayer(this)

    }

    fun onEvent( event: OpenDrawerEvent){
        drawer.open()
    }

    private fun makeCurrentFragment(container: FragmentContainerView, fragmentClass: Class<out Fragment>) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(container.id, fragmentClass, null, fragmentClass.name)
        }
    }

    override fun onDestroy() {
        Log.i(Logs.LIFECYCLE.toString(),"MusicPlayer activity destroyed")

        bus.unregister(this)

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        AskForPermissionsAtStart( this,GlobalValues.System.RequiredPermissions.subList(0,2))

    }







}