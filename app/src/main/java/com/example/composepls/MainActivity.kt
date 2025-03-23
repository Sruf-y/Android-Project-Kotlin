package com.example.composepls

import android.annotation.TargetApi
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.ActivityOptions
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.transition.TransitionInflater
import android.view.View
import android.view.Window
import androidx.activity.addCallback
import androidx.annotation.NavigationRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


@TargetApi(33)
class MainActivity : AppCompatActivity() {

    lateinit var toolBar:Toolbar
    lateinit var navBar:BottomNavigationView
    var savedPage:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
//            insets
//        }


        //toolBar = findViewById(R.id.myToolbar)
        //setSupportActionBar(toolBar);

        navBar = findViewById(R.id.bottomNavigationView);



        setSystemBarToSwipeUp(window)
        adjustViewsForKeyboard(navBar);



        navBar.setOnItemSelectedListener { item ->
//            if(item.itemId!=R.id.time){
//                recycleState=null
//                verticaloffset=0
//            }

            when (item.itemId) {
                R.id.sound -> {
                    R.string.page_number=-1

                    savedPage=R.id.sound

                    makeCurrentFragment(BlankFragment2());
                    true //return true like the break in a switch
                }
                R.id.home -> {
                    R.string.page_number=0

                    savedPage=R.id.home

                    makeCurrentFragment(BlankFragment())
                    true
                }
                R.id.time -> {
                    R.string.page_number=1

                    savedPage=R.id.time

                    makeCurrentFragment(BlankFragment3())

                    true
                }
                else -> false // Return false if the item selection is not handled
            }
        }


        //return to home if its in another fragment
        onBackPressedDispatcher.addCallback(this) {
            if(R.string.page_number!=0) {
                    R.string.page_number = 0;
                    navBar.selectedItemId=R.id.home;
                    makeCurrentFragment(BlankFragment());
            }
            else
            {
                finish();
            }

        }



        //useless for now, might need. If later than February of 2025, then remove this
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.no_transition)
        window.sharedElementReturnTransition = TransitionInflater.from(this)
            .inflateTransition((android.R.transition.no_transition))
    }

    override fun onStart() {
        super.onStart()

        if(savedPage!=0)
        {
            navBar.selectedItemId=savedPage;
        }
        else
        {
            navBar.selectedItemId=R.id.home;
        }

    }

    override fun onPause() {
        super.onPause()

        savedPage=navBar.selectedItemId;

        var sf: SharedPreferences;
        var editor : SharedPreferences.Editor;

        sf=this.getSharedPreferences("My SF",Context.MODE_PRIVATE);
        editor=sf.edit();

        editor.apply(){
            putInt("SF_page",savedPage);
            commit();
        }


        //overridePendingTransition(R.anim.from_bellow,R.anim.to_up)



    }
    override fun onResume() {
        super.onResume()

        var sf: SharedPreferences;
        var editor : SharedPreferences.Editor;

        sf=this.getSharedPreferences("My SF",Context.MODE_PRIVATE);
        editor=sf.edit();


        savedPage=sf.getInt("SF_page",savedPage);

        navBar.selectedItemId=savedPage;
        // transition animations with onPendingTransition go into the onPause of the previous ACTIVITY !


    }

    override fun onStop() {
        super.onStop()

    }





    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView2,fragment)
            commit();
        }
    }



}




@RequiresApi(Build.VERSION_CODES.R)
fun setSystemBarToSwipeUp(window:Window){
    //val imecontroller = this.window.insetsController
    //imecontroller?.systemBarsBehavior

    val barscontroller = WindowInsetsControllerCompat(window,window.decorView)

    barscontroller.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    barscontroller.hide(WindowInsetsCompat.Type.navigationBars())

    //val notificationbarcontroller=window.insetsController
    //notificationbarcontroller?.hide(WindowInsetsCompat.Type.statusBars())


}

fun adjustViewsForKeyboard(view:View){
    ViewCompat.setOnApplyWindowInsetsListener(view){v,insets->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

        val systembarInsets=insets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.setPadding(0,0,0,systembarInsets.bottom)

        insets
    }
}




fun disable(button:Button){
    button.isEnabled=false;
}
fun enable(button:Button){
    button.isEnabled=true;
}

fun uninstall(context: Context, packageName:String){
    Log.i("MYTAG","Main activity destroy");

    val packageName = packageName // Get the current app's package name
    val uninstall = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
    context.startActivity(uninstall);
}

//fun customSnack(whereToShowIt: View, context: Context, message: String){
//    val snack = Snackbar.make(whereToShowIt,message, Snackbar.LENGTH_SHORT);
//    snack.view.setBackgroundColor(ContextCompat.getColor(context,R.color.gray_dark));
//    val snackText = snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text);
//    snackText.setTextColor(ContextCompat.getColor(context,R.color.white));
//
//    snack.show();
//}

fun customToast(whereToShowIt: View, context: Context, message: String) {
    val tost = Toast.makeText(context, message, Toast.LENGTH_SHORT);


    tost.show();
}



fun sendPageToLeft(context: Context):Bundle{

    val aux = ActivityOptions.makeCustomAnimation(
        context,
        R.anim.slide_from_right,
        R.anim.slide_to_left
    )
    return aux.toBundle();
}

fun sendPageToRight(context: Context):Bundle{
    val aux= ActivityOptions.makeCustomAnimation(
        context,
        R.anim.slide_from_left,
        R.anim.slide_to_right
    )
    return aux.toBundle();
}


