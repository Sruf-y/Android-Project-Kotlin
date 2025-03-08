package Adaptors

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import Adaptors.Utils.Companion.dP
import com.example.composepls.R
import Classes_Ojects.alarmViewModel
import com.example.composepls.doingselection
import com.example.composepls.toBool
import com.google.android.material.checkbox.MaterialCheckBox


class alarmAdapter(val mList:List<alarmViewModel>, val listener: OnSwitchListener, val listener2: onCardClickListener, val listener3: onCardLongPressListener):RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    interface OnSwitchListener {
        fun onSwitch(position: Int,itemviewholder: ItemViewHolder)
    }
    interface onCardClickListener{
        fun oncardClick(position:Int,itemviewholder: ItemViewHolder)
    }
    interface onCardLongPressListener{
        fun onCardLongPress(position:Int,itemviewholder: ItemViewHolder)
    }

    //holds the header ONLY

//    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val title = itemView.findViewById<TextView>(R.id.`when`)
//        val subtitle = itemView.findViewById<TextView>(R.id.whatexacttime)
//    }


    //holds the items of the recycleview
    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val timp: TextView = itemView.findViewById(R.id.allarmTime)
        val am:TextView= itemView.findViewById(R.id.AMPM);
        val swich: Switch = itemView.findViewById(R.id.state)
        val mySelector:CheckBox = itemView.findViewById(R.id.checkBox)
        val title:TextView=itemView.findViewById(R.id.alarmTitle)
        val card:ConstraintLayout=itemView.findViewById(R.id.cardView2)
        val Luni:MaterialCheckBox = itemView.findViewById(R.id.smallLuni)
        val Marti:MaterialCheckBox=itemView.findViewById(R.id.smallMarti)
        val Miercuri:MaterialCheckBox=itemView.findViewById(R.id.smallMiercuri)
        val Joi:MaterialCheckBox=itemView.findViewById(R.id.smallJoi)
        val Vineri:MaterialCheckBox=itemView.findViewById(R.id.smallVineri)
        val Sambata:MaterialCheckBox=itemView.findViewById(R.id.smallSambata)
        val Duminica:MaterialCheckBox=itemView.findViewById(R.id.smallDuminica)
        val days:LinearLayout=itemView.findViewById(R.id.days)
        val isTimed:ImageView=itemView.findViewById(R.id.istimed)
        val weekly_card:LinearLayout=itemView.findViewById(R.id.days)


    }


    //asks what type they are
//    override fun getItemViewType(position: Int): Int {
//        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
//    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if(viewType == VIEW_TYPE_HEADER){
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.alarmheader, parent, false)
//
//        HeaderViewHolder(view)
//        }
//        else{
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.alarm_example, parent, false)
//            ItemViewHolder(view)
//        }

        val view=LayoutInflater.from(parent.context).inflate(R.layout.alarm_example,parent,false)

        return ItemViewHolder(view);
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        if (holder is ItemViewHolder) {
            val itemCard = mList[position]

            var timptext:String=itemCard.ora.toString()
            if(itemCard.ora==0){
                timptext="12"
            }
            if(itemCard.minute>10)
                timptext += ":${itemCard.minute}";
            else
                timptext+= ":0${itemCard.minute}";
            holder.timp.text=timptext
            holder.am.text = itemCard.aM;
            holder.swich.isChecked = itemCard.active
            holder.mySelector.isChecked=itemCard.editChecker

            if(mList[position].properties[0].isBlank())
            {
                holder.title.layoutParams?.height=1.dP
                holder.title.visibility=View.INVISIBLE
            }
            else
            {

                holder.title.text=mList[position].properties[0]
                holder.title.visibility=View.VISIBLE
                holder.title.layoutParams?.height=ViewGroup.LayoutParams.WRAP_CONTENT
            }

            if(mList[position].type[0]%10==2)
            {

                holder.Luni.isChecked=mList[position].type[1].toBool()
                holder.Marti.isChecked=mList[position].type[2].toBool()
                holder.Miercuri.isChecked=mList[position].type[3].toBool()
                holder.Joi.isChecked=mList[position].type[4].toBool()
                holder.Vineri.isChecked=mList[position].type[5].toBool()
                holder.Sambata.isChecked=mList[position].type[6].toBool()
                holder.Duminica.isChecked=mList[position].type[7].toBool()
            }
            else{
                holder.Luni.isChecked=false
                holder.Marti.isChecked=false
                holder.Miercuri.isChecked=false
                holder.Joi.isChecked=false
                holder.Vineri.isChecked=false
                holder.Sambata.isChecked=false
                holder.Duminica.isChecked=false
            }

            if(mList[position].type[0]>10)
            {
                holder.isTimed.visibility=View.VISIBLE
            }
            else
            {
                holder.isTimed.visibility=View.INVISIBLE
            }

            





            if(itemCard.active)
            {
                holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.white
                ))
                holder.timp.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.white
                ))
                holder.am.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.white
                ))
            }
            else
            {
                holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.inactive
                ))
                holder.timp.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.inactive
                ))
                holder.am.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.inactive
                ))
            }







            when(doingselection)
            {
                0->{
                    holder.mySelector.isChecked=itemCard.editChecker
                    holder.mySelector.translationX=-34.dP.toFloat()
                    holder.swich.translationX=0.dP.toFloat()
                    holder.timp.translationX=0.dP.toFloat()
                    holder.title.translationX=0.dP.toFloat()
                    holder.am.translationX=0.dP.toFloat()
                    holder.days.translationX=0.dP.toFloat()


                }
                1->{
                    holder.timp.animate()
                        .setDuration(200)
                        .translationX(34.dP.toFloat())
                        .apply {}
                        .start()

                    holder.title.animate()
                        .setDuration(200)
                        .translationX(34.dP.toFloat())
                        .apply {}
                        .start()

                    holder.am.animate()
                        .setDuration(200)
                        .translationX(34.dP.toFloat())
                        .apply {}
                        .start()

                    holder.swich.animate()
                        .setDuration(200)
                        .translationX(80.dP.toFloat())
                        .apply {

                    }.start()

                    holder.mySelector.fadeIn()
                    holder.mySelector.animate()
                        .setDuration(200)
                        .translationX(0.dP.toFloat())
                        .apply {}
                        .start()

                    holder.days.animate()
                        .setDuration(200)
                        .translationX(55.dP.toFloat())
                        .apply {}
                        .start()


                }
                2->{
                    holder.timp.animate()
                        .setDuration(200)
                        .translationX(0.dP.toFloat())
                        .apply {}
                        .start()

                    holder.title.animate()
                        .setDuration(200)
                        .translationX(0.dP.toFloat())
                        .apply {}
                        .start()

                    holder.am.animate()
                        .setDuration(200)
                        .translationX(0.dP.toFloat())
                        .apply {}
                        .start()


                    holder.swich.animate()
                        .setDuration(200)
                        .translationX(0.dP.toFloat())
                        .apply {}
                        .start()

                    holder.mySelector.fadeOut()
                    holder.mySelector.animate()
                        .setDuration(200)
                        .translationX(-34.dP.toFloat())
                        .apply {

                        }
                        .start()

                    holder.days.animate()
                        .translationX(0.dP.toFloat())
                        .setDuration(200)
                        .apply{}
                        .start()



                    mList[position].editChecker=false
                    holder.mySelector.isChecked=false



                }
            }

















            holder.swich.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_MOVE) {
                    return@setOnTouchListener true // Block dragging
                }
                false // Allow other interactions (click)
            }

            holder.swich.setOnClickListener {
                listener.onSwitch(position, holder)
            }

            holder.itemView.setOnClickListener{
                listener2.oncardClick(position,holder)
            }

            holder.itemView.setOnLongClickListener {
                listener3.onCardLongPress(position,holder)
                true
            }

        }


    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        holder.itemView.findViewById<CheckBox>(R.id.smallLuni).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallMarti).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallMiercuri).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallJoi).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallVineri).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallSambata).setOnCheckedChangeListener(null)
        holder.itemView.findViewById<CheckBox>(R.id.smallDuminica).setOnCheckedChangeListener(null)
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}



class Utils {
    companion object {
        val Int.dP: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        val Float.dP: Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

        val Double.dP:Int
            get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    }
}


fun View.fadeOut(duration: Long = 100) {
    animate()
        .alpha(0f) // Fade out to 0 opacity
        .setDuration(duration)
        .withEndAction {
            this.visibility = View.GONE // Optional: Hide the view after fade-out
        }
}

fun View.fadeIn(duration:Long=100)
{
    animate()
        .alpha(1F)
        .setDuration(duration)
        .withEndAction{
            this.visibility=View.VISIBLE
        }
}
