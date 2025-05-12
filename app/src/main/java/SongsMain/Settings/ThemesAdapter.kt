package SongsMain.Settings

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.composepls.R

class ThemesAdapter(val context: Context, var mList: ArrayList<Int>,var onClick:((drawable:Int,position:Int)->Unit)): BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)


    inner class Theme(view:View){
        val imageview = view.findViewById<ImageView>(R.id.main)

        fun bind(drawable:Int,position: Int){

            imageview.setImageResource(drawable)

            imageview.setOnClickListener {
                onClick(drawable,position)
            }
        }
    }



    override fun getCount(): Int {
        return mList.size
    }

    override fun getItem(p0: Int): Int? {
        return mList[p0]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        vieW: View?,
        viewgroup: ViewGroup?
    ): View? {

        var view:View
        var holder: Theme

        if(vieW == null){

            view = inflater.inflate(R.layout.theme_example,viewgroup,false)
            holder = Theme(view)
            view.tag=holder
        }else{
            view = vieW
            holder = view.tag as Theme
        }




        holder.bind(mList[position],position)







        return view

    }


}