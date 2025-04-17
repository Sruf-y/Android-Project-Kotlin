package SongsMain.Tutorial
import GlobalValues.Media.CHANNEL_NAME
import GlobalValues.Media.CHANNEL_ID
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class BaseApplication: Application() {




    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }
}