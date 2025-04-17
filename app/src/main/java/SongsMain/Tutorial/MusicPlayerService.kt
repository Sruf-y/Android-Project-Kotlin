package SongsMain.Tutorial

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.security.Provider

class MusicPlayerService: Service() {

    val binder = MusicBinder()

    inner class MusicBinder:Binder(){

    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }



}