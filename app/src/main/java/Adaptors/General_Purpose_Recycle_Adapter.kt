package Adaptors


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.composepls.R

class General_Purpose_Recycle_Adapter<T>(val mList: List<T>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface onClickListener{
        fun setOnCardClickListener(position:Int,itemViewHolder: View)
    }

    interface onLongPressListener{
        fun setOnCardLongPressListener(position:Int,itemViewHolder: View)
    }



    class directory(itemView: View): RecyclerView.ViewHolder(itemView){

    }

    class file(itemView: View): RecyclerView.ViewHolder(itemView){

    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View

        //view = LayoutInflater.from(parent.context).inflate(R.layout.)
        TODO()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}