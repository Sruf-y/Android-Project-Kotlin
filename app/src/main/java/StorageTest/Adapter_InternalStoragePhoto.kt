package StorageTest

import Adaptors.alarmAdapter
import StorageTest.Classes.InternalStoragePhoto
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.composepls.R
import DataClasses_Ojects.ViewAttributes
import Functions.loadImageWithColorTransition
import StorageTest.Classes.ExternalStoragePhoto
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.animation.core.animate
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.AccelerateInterpolator
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.DecelerateInterpolator
import androidx.core.animation.LinearInterpolator
import androidx.core.animation.ValueAnimator
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import kotlin.coroutines.coroutineContext
import androidx.core.graphics.drawable.toDrawable
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class Adapter_InternalStoragePhoto(val mList:ArrayList<InternalStoragePhoto>, var context:Context, screensize:Point, val listener: onClickListener, val listener2: onLongPressListener):
    Adapter<ViewHolder>(){



    interface onClickListener {
        fun onPictureClick(position: Int,itemViewHolder: ViewHolder)
    }

    interface onLongPressListener{
        fun onPictureLongClick(position: Int,itemViewHolder: ViewHolder)
    }



    class Photo(itemView: View): ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.imageView)
        val constraintLayout = itemView.findViewById<ConstraintLayout>(R.id.constr)


    }




fun updateData( lista:ArrayList<InternalStoragePhoto>){
    mList.clear()
    mList.addAll(lista)
    notifyDataSetChanged()

}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.photo_example,parent,false)

        return Photo(itemview)
    }



    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val photo = mList[position]



        (holder as Photo)

        val aspectRadio = photo.bitmap.width.toFloat() / photo.bitmap.height.toFloat()

        ConstraintSet().apply {
            clone(holder.constraintLayout)
            setDimensionRatio(holder.imageView.id, aspectRadio.toString())
            applyTo(holder.constraintLayout)
        }
        holder.imageView.setImageBitmap(photo.bitmap)


        //usage
        loadImageWithColorTransition(
            holder.imageView,
            R.color.semi_transparent,
            R.color.transparent,
            200L,
            photo.bitmap
        )






        holder.itemView.setOnClickListener {
            blipCard(position,holder)
            true
        }
        holder.itemView.setOnLongClickListener {
            i->listener2.onPictureLongClick(position,holder)
            true
        }
    }



    override fun getItemCount(): Int {
        return mList.size
    }

    fun removeAt(position: Int){
    mList.removeAt(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(position, mList.size);
    }

    fun insertAt(position:Int,data: InternalStoragePhoto){
        mList.add(position, data)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, mList.size);
    }

    fun saveToJson(filename:String){
        Functions.saveAsJson(context, filename, mList)
    }

    fun flashCard(position: Int){

    }

    fun blipCard(position: Int, holder: ViewHolder) {
        Functions.blipImage(
            (holder as Photo).imageView,
            R.color.activated,
            R.color.transparent,
            mList[position].bitmap,
            1000L,
            DecelerateInterpolator()
        )
    }



}