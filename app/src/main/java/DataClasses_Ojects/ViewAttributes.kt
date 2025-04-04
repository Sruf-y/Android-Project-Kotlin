package DataClasses_Ojects

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat.getColor
import com.example.composepls.R
import android. content. Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModel
import com.google.android.material.imageview.ShapeableImageView
import org.xmlpull.v1.XmlPullParser


class ViewAttributes(public val view:View):Object(){


    inner class ForegroundTint{
        public fun Get(): ColorStateList? {
            return view.foregroundTintList
        }

        public fun Compare(context:Context,colorId:Int):Boolean{
            return view.foregroundTintList==ColorStateList.valueOf(getColor(context,colorId))
        }

        public fun Set(context:Context,colorId:Int){
            view.foregroundTintList=ColorStateList.valueOf(getColor(context,colorId))
        }
    }

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

    inner class Background{
        public fun Get(): Drawable? {
            return view.background
        }

        public fun Compare(context:Context,drawableId:Int):Boolean{
            return view.background== ContextCompat.getDrawable(context,drawableId)
        }

        public fun Set(context:Context,drawableId:Int){
            view.background=ContextCompat.getDrawable(context,drawableId)
        }
    }

    inner class BackgroundTintMode{
        public fun Get(): PorterDuff.Mode? {
            return view.backgroundTintMode
        }

        public fun Compare(PorterduffMode: PorterDuff.Mode):Boolean{
            return view.backgroundTintMode==PorterduffMode
        }

        public fun Set(PorterduffMode: PorterDuff.Mode){
            view.backgroundTintMode= PorterduffMode
        }
    }

    inner class Edges{
        var Bottom:Int
            get() {return view.bottom}
            set(value:Int){view.bottom=value}

        var Top:Int
            get() {return view.top}
            set(value) {view.top=value}

        var Left: Int
            get() {return view.left}
            set(value:Int){view.left=value}

        var Right: Int
            get() {return view.right}
            set(value:Int){view.right=value}
        var TopLeft: List<Int>
            get() {return listOf(view.top, view.left)}
            set(value:List<Int>){
                if(value.size==2)
                view.top=value[0]
                view.left=value[1]
            }
        var TopRight: List<Int>
            get() {return listOf(view.top, view.right)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.top = value[0]
                    view.right = value[1]
                }
            }
        var BottomLeft: List<Int>
            get() {return listOf(view.bottom,view.left)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.bottom = value[0]
                    view.left = value[1]
                }
            }
        var BottomRight: List<Int>
            get() {return listOf(view.bottom,view.right)}
            set(value:List<Int>){
                if(value.size==2) {
                    view.bottom = value[0]
                    view.right = value[1]
                }
            }


    }
}