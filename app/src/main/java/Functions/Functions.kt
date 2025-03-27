package Functions

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.composepls.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


@Composable
fun HideKeyboard(){
    //val keyboardController= LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusmanager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Hide the keyboard when the "Done" action is performed
                    keyboardController?.hide()
                    focusmanager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Hide the keyboard when the button is clicked
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hide Keyboard")
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

fun CustomSnack(whereToShowIt: View, message: String){
    val snack = Snackbar.make(whereToShowIt,message, Snackbar.LENGTH_SHORT)
    snack.animationMode=Snackbar.ANIMATION_MODE_FADE
    snack.apply {
        view.setOnClickListener {
            dismiss()
        }

        behavior=object:BaseTransientBottomBar.Behavior() {
            override fun canSwipeDismissView(child: View): Boolean {
                return true
            }
        }

        (view.layoutParams as FrameLayout.LayoutParams).apply {
            width= ActionBar.LayoutParams.WRAP_CONTENT;
            gravity= Gravity.END or Gravity.BOTTOM;
        }

        view.apply {
            setBackgroundColor( ContextCompat.getColor(context,R.color.gray))

            setAnchorView(whereToShowIt)

        }
    }.show()
}

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


fun saveAsJson(context: Context, filename:String, data:Any) {
    val json=Gson().toJson(data);
    val filepath=context.filesDir.toString()+"/"+filename+".json"

    File(filepath).writeText(json)
}

inline fun <reified T> loadFromJson(context:Context,filename: String,data: T): T {

    val filepath=context.filesDir.toString()+"/"+filename+".json"
    val file=File(filepath)

    if(file.exists()) {
        val loadedFile = file.readText()
        val type = object:TypeToken<T>(){}.type
        val readData:T = Gson().fromJson(loadedFile, type)

        return readData
    }
    return data
}