package DataClasses_Ojects

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat.getColor
import com.example.composepls.R
import android. content. Context



class ViewAttributes(public val view:View){


    inner class BackgroundTint{
        public fun Get(): ColorStateList? {
            return view.backgroundTintList
        }

        public fun Compare(context:Context,colorId:Int):Boolean{
            return view.backgroundTintList==ColorStateList.valueOf(getColor(context,colorId))
        }

        public fun Set(context:Context,colorId:Int){
            view.backgroundTintList=ColorStateList.valueOf(getColor(context,colorId))
        }
    }

    inner class BackgroundResource{

    }

}