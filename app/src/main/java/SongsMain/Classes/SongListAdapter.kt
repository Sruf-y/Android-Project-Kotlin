package SongsMain.Classes

import DataClasses_Ojects.Logs
import StorageTest.Classes.Tip_For_adaptor
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.HardwarePropertiesManager
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RestrictTo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.load.resource.bitmap.HardwareConfigState
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.composepls.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import kotlin.io.encoding.Base64
import kotlin.toString

class SongListAdapter(var mList:ArrayList<Song>, val context:Context,var onItemClick:(Song)->Unit,var onItemLongPress:(Song)->Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    private val differ = AsyncListDiffer<Song>(this, SongDiffUtilsCallback())




    inner class itemInList(itemView:View):RecyclerView.ViewHolder(itemView){
        val displayTitle: TextView=itemView.findViewById(R.id.title)
        val displayAuthor:TextView=itemView.findViewById(R.id.author)
        val displayImageView: ImageView=itemView.findViewById(R.id.thumbnail)
        val displayOptionsButton: ImageView=itemView.findViewById(R.id.songOptions)
        val movementHandle: ImageView=itemView.findViewById(R.id.movementHandle)

         fun bind(song:Song){

            displayTitle.text = song.title

            displayAuthor.text = song.author

            displayImageView.setImageDrawable(null)

            Glide.with(itemView)
                .asBitmap()
                .load(File(SongsGlobalVars.musicDirectory(context),song.songUri.toUri().lastPathSegment.toString()+".jpg"))
                //.diskCacheStrategy(DiskCacheStrategy.ALL) // caching pictures to not reload them every single time
                .skipMemoryCache(true)
                .placeholder(R.drawable.blank) // Show while loading
                .error(R.drawable.blank_gray) // Show if load fails
                .transition(BitmapTransitionOptions.withCrossFade())
                //.dontAnimate() // Optional: prevents crossfade
                //.onlyRetrieveFromCache(true)
                .into(displayImageView)

//            if(song.thumbnail!=null)
//                displayImageView.setImageBitmap(song.thumbnail)



            itemView.setOnClickListener {
                song.lastPlayed = LocalDateTime.now()
                song.timesListened++;


                onItemClick(song)
            }

            itemView.setOnLongClickListener {


                onItemLongPress(song)
                true
            }
        }
    }


    class SongDiffUtilsCallback: DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem.songUri==newItem.songUri
        }

        override fun areContentsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem==newItem
        }

        override fun getChangePayload(
            oldItem: Song,
            newItem: Song
        ): Any? {
            val diff = Bundle()

            //if(oldItem.thumbnail!=newItem.thumbnail) diff.putParcelable("thumbnail",newItem.thumbnail)

            return if(diff.size()==0)null else diff
        }
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


        (holder as itemInList).apply {


            bind(mList[position])




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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        (holder as itemInList).displayImageView.setImageBitmap(null)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any?>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Handling partial updates
            val song = differ.currentList[position]
            val combinedPayload = Bundle()

            (holder as itemInList).apply {
                payloads.forEach { payload ->
                    if (payload is Bundle) {
                        combinedPayload.putAll(payload)
                    }

                    if (combinedPayload.containsKey("title")) {
                        holder.displayTitle.text = song.title
                    }
                    if (combinedPayload.containsKey("artist")) {
                        holder.displayAuthor.text = song.author
                    }
                    if (combinedPayload.containsKey("artworkUri")) {
//                        Glide.with(holder.itemView)
//                            .load(song.artworkUri)
//                            .transition(DrawableTransitionOptions.withCrossFade())
//                            .into(holder.artworkImageView)
                    }

                }
            }
        }

    }




        fun removeAt(position: Int) {
            mList.removeAt(position);
            notifyItemRemoved(position);

        }

        fun insertAt(position: Int, data: Song) {
            mList.add(position, data)
            notifyItemInserted(position)
            notifyItemRangeChanged(0, mList.size)

        }

        fun clear() {
            val size = mList.size
            mList.clear()
            notifyItemRangeChanged(0, size)
        }

        fun dump(): ArrayList<Song> {
            val size = mList.size
            val tempList = mList
            mList.clear()
            notifyItemRangeChanged(0, size)

            return tempList
        }

        fun updateData(lista: ArrayList<Song>) {
            mList.clear()
            mList.addAll(lista)
            notifyDataSetChanged()

        }


        override fun getItemCount(): Int {
            return mList.size
        }


}