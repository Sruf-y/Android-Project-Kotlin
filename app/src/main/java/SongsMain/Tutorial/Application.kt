package SongsMain.Tutorial
import SongsMain.Classes.SongsGlobalVars
import SongsMain.Classes.SongsGlobalVars.CHANNEL_NAME
import SongsMain.Classes.SongsGlobalVars.CHANNEL_ID
import android.app.Application
import android.app.NotificationChannel
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

class Application: Application(), LifecycleObserver {




    override fun onCreate() {
        super.onCreate()

        SongsGlobalVars

        createNotificationChannel()
        instance = this
    }



    companion object {
        lateinit var instance: Application
            private set
    }

    fun createNotificationChannel(){
        val MusicChannel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(MusicChannel)
    }


}