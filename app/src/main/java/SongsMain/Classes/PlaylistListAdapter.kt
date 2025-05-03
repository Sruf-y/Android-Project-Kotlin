package SongsMain.Classes


import SongsMain.Tutorial.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.composepls.R
import de.greenrobot.event.EventBus
import java.io.File




class PlaylistListAdapter(var mList:ArrayList<Playlist>, val context:Context, var onItemClick:(Playlist)->Unit, var onItemLongPress:(Playlist)->Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }




    private  val bus: EventBus = EventBus.getDefault()






    inner class itemInList(itemView:View):RecyclerView.ViewHolder(itemView){
        val displayTitle: TextView=itemView.findViewById(R.id.title)
        val displayImageView: ImageView=itemView.findViewById(R.id.thumbnail)
        val displayOptionsButton: ImageView=itemView.findViewById(R.id.songOptions)
        val movementHandle: ImageView=itemView.findViewById(R.id.movementHandle)

        var myPosition=-1







        fun bind(playlist: Playlist, position: Int){

            myPosition=position


            displayTitle.text = playlist.title
            displayTitle.setTextColor(ContextCompat.getColor(Application.instance, R.color.white))







            playlist.thumbnail.apply{



                if (this!=null) {
                    Glide.with(itemView)
                        .asBitmap()
                        .load(this)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // caching pictures to not reload them every single time
                        //.skipMemoryCache(true)
                        .placeholder(R.drawable.blank) // Show while loading
                        .error(R.drawable.blank_gray_musical_note) // Show if load fails
                        .transition(BitmapTransitionOptions.withCrossFade(500))
                        .into(displayImageView)

                } else {
                    Glide.with(itemView)
                        .asDrawable()
                        .load(R.drawable.blank_gray_musical_note)
                        .transition(DrawableTransitionOptions.withCrossFade(0))
                        .into(displayImageView)
                }
            }


//             if(!bus.isRegistered(this@itemInList))
//             {
//                 bus.register(this@itemInList)
//             }




            itemView.setOnClickListener {


                onItemClick(playlist)
            }

            itemView.setOnLongClickListener {


                onItemLongPress(playlist)
                true
            }
        }
    }









    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_example,parent,false)



        return itemInList(view)
    }



    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {


        (holder as itemInList).apply {


            bind(mList[position],position)




        }




        // FOR MAKING THE PLAYLIST ADAPTER

//                    when(playlist.title){
//                        "My Favourite"->{
//                            // TODO make the playlist picture into the picture i want or just a generic musical note picture otherwise
//                        }
//                        "Recently played"->{}
//                        "Recently downloaded"->{}
//                        else->{}
//                    }



    }





    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)

    }



    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        (holder as itemInList).apply {
            displayImageView.setImageBitmap(null)
        }


    }








    fun removeAt(position: Int) {
        mList.removeAt(position);
        notifyItemRemoved(position);

    }

    fun insertAt(position: Int, data: Playlist) {
        mList.add(position, data)
        notifyItemInserted(position)
        notifyItemRangeChanged(0, mList.size)

    }

    fun clear() {
        val size = mList.size
        mList.clear()
        notifyItemRangeChanged(0, size)
    }

    fun dump(): ArrayList<Playlist> {
        val size = mList.size
        val tempList = mList
        mList.clear()
        notifyItemRangeChanged(0, size)

        return tempList
    }

    fun updateData(lista: ArrayList<Playlist>) {
        mList.clear()
        mList.addAll(lista)
        notifyDataSetChanged()

    }


    override fun getItemCount(): Int {
        return mList.size
    }


}