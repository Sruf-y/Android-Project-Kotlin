package SongsMain

import DataClasses_Ojects.Logs
import Functions.AskForPermissionsAtStart
import Functions.OpenAppSettings
import Functions.VerifyPermissions
import SongsMain.Classes.myMediaPlayer
import Utilities.Utils.Companion.dP
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMarginsRelative
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.composepls.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import de.greenrobot.event.EventBus
import de.greenrobot.event.ThreadMode
import kotlin.math.max


class OpenDrawerEvent()




class SongsMain_view : AppCompatActivity() {

    val bus:EventBus = EventBus.getDefault();

    lateinit var fragmentContainer: FragmentContainerView
    lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setContentView(R.layout.activity_songs_main_view)






        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))
        drawer=findViewById(R.id.drawerLayout)

        enableEdgeToEdge(myStatusBarStyle,myNavigationBarStyle)

        val navView: NavigationView= requireViewById(R.id.navView)


        fragmentContainer=findViewById(R.id.fragmentContainerView)


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


        makeCurrentFragment(fragmentContainer,SongsMain.SongsMain_Base())












        bus.register(this)
        myMediaPlayer.initializeMediaPlayer(this)


    }

    fun onEvent( event: OpenDrawerEvent){
        drawer.open()
    }

    private fun makeCurrentFragment(container:FragmentContainerView,fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(container.id,fragment)
            commit();
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