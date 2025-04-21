package SongsMain

import SongsMain.Tabs.Playlists
import SongsMain.Tabs.SimpleSongList
import SongsMain.Tabs.SongArtists
import SongsMain.Tabs.SongFolders
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.LocalTime

class TabSwipeAdaptor(fragment: Fragment) : FragmentStateAdapter(fragment) {
    // Fragments will be preserved by FragmentStateAdapter
    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment = getFragment(position)



    private fun getFragment(position: Int):Fragment{

        Log.i("TESTS","A fragment was created for the tabs menu! +${LocalTime.now()}")

        return when (position) {

            0 -> GlobalValues.Media.SimpleSongList
            1 -> GlobalValues.Media.Playlists
            2 -> GlobalValues.Media.SongFolders
            3 -> GlobalValues.Media.SongArtists
            else -> throw IllegalArgumentException()
        }
    }
    // Optional: If you need to identify fragments
    override fun getItemId(position: Int): Long {
        return position.toLong() // Default implementation is sufficient
    }
}