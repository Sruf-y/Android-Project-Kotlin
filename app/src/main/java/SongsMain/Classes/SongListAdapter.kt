package SongsMain.Classes

import StorageTest.Classes.Tip_For_adaptor
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.composepls.R

class SongListAdapter<T>(var mList:ArrayList<T>, val tip: Tip_For_adaptor, val context:Context, val clickListener: onClickListener, val longClickListener: onLongPressListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface onClickListener{
        fun setOnCardClickListener(position:Int,itemViewHolder: RecyclerView.ViewHolder)
    }

    interface onLongPressListener{
        fun setOnCardLongPressListener(position:Int,itemViewHolder:RecyclerView.ViewHolder)
    }



    class itemInList(itemView:View):RecyclerView.ViewHolder(itemView){
        val displayTitle: TextView=itemView.findViewById(R.id.title)
        val displayAuthor:TextView=itemView.findViewById(R.id.author)
        val displayImageView: ImageView=itemView.findViewById(R.id.thumbnail)
        val displayOptionsButton: ImageView=itemView.findViewById(R.id.songOptions)
        val movementHandle: ImageView=itemView.findViewById(R.id.movementHandle)
    }





    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_example,parent,false)

        return itemInList(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when(tip){
            Tip_For_adaptor.song -> {

                val song = mList[position] as Song
                (holder as itemInList).apply {


                    Glide.with(context)
                        .load(song.thumbnail)
                        .placeholder(R.drawable.blank) // Show while loading
                        .error(R.drawable.blank_gray) // Show if load fails
                        .dontAnimate() // Optional: prevents crossfade
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // caching pictures to not reload them every single time
                        .into(displayImageView)



                    displayTitle.text=song.title

                    displayAuthor.text=song.author



                }
            }
            Tip_For_adaptor.playlist -> {

                val playlist = mList[position] as Playlist
                (holder as itemInList).apply {
                    if(playlist.isUserOrdered){
                        movementHandle.visibility= View.VISIBLE
                    }


                    when(playlist.title){
                        "My Favourite"->{
                            // TODO make the playlist picture into the picture i want or just a generic musical note picture otherwise
                        }
                        "Recently played"->{}
                        "Recently downloaded"->{}
                        else->{}
                    }

                    displayTitle.text=playlist.title

                    if (playlist.songsList != null) {
                        displayAuthor.text = playlist.songsList!!.size.toString()
                    } else {
                        displayAuthor.text = "0"
                    }




                }
            }
        }














        holder.itemView.setOnClickListener{

//            if (tip == Tip_For_adaptor.song)
//                (mList[position] as Song).apply {
//                    lastPlayed = LocalDateTime.now()
//                    timesListened++;
//                }

            clickListener.setOnCardClickListener(position, holder)
        }

        holder.itemView.setOnLongClickListener {
            longClickListener.setOnCardLongPressListener(position,holder)

            true
        }
    }







    fun removeAt(position: Int) {
        mList.removeAt(position);
        notifyItemRemoved(position);

    }

    fun insertAt(position:Int,data: T){
        mList.add(position, data)
        notifyItemInserted(position)
        notifyItemRangeChanged(0,mList.size)

    }

    fun clear(){
        val size = mList.size
        mList.clear()
        notifyItemRangeChanged(0,size)
    }

    fun dump(): ArrayList<T>{
        val size = mList.size
        val tempList = mList
        mList.clear()
        notifyItemRangeChanged(0,size)

        return tempList
    }

    fun updateData(lista: ArrayList<T>) {
        mList.clear()
        mList.addAll(lista)
        notifyDataSetChanged()

    }









    override fun getItemCount(): Int {
        return mList.size
    }
}