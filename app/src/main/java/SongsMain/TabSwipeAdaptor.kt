package SongsMain

import SongsMain.Tabs.Playlists
import SongsMain.Tabs.SimpleSongList
import SongsMain.Tabs.SongArtists
import SongsMain.Tabs.SongFolders
import android.R
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabSwipeAdaptor(fragment:Fragment): FragmentStateAdapter(fragment) {

    val fragmentList = listOf<Fragment>(SimpleSongList(), Playlists(), SongFolders(), SongArtists())

    override fun createFragment(position: Int): Fragment {

        if(position>=0 && position < fragmentList.size){
            return fragmentList[position]
        }
        else return fragmentList[0]

    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}