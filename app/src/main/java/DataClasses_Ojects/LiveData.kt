package DataClasses_Ojects

import SongsMain.Classes.myMediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Runnable
import java.time.LocalDateTime




class LiveData<T>(
    initialValue: T,
    private val updateIntervalMs: Long = 1000,
    private val updateAction: ((currentValue: T) -> Unit)? = null
) : ViewModel() {

    private val _value = MutableLiveData<T>(initialValue)
    val value: LiveData<T> get() = _value

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            updateAction?.invoke(_value.value!!)
            handler.postDelayed(this, updateIntervalMs)
        }
    }

    init {
        handler.post(runnable)
    }

    fun startUpdateWatching(){
        handler.post(runnable)
    }

    fun update(newValue: T) {
        _value.value = newValue
    }

    override fun onCleared() {
        handler.removeCallbacks(runnable)
        super.onCleared()
    }
}


class MediaProgressViewModel : ViewModel() {
    private val _progress = MutableLiveData<Int>(0)
    val progress: LiveData<Int> = _progress

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 250) // Update every 250ms
        }
    }

    private fun updateProgress() {
        val currentPos = myMediaPlayer.getCurrentPosition()
        val duration = myMediaPlayer.currentlyPlayingSong?.duration ?: 0

        val progress = if (duration > 0) {
            currentPos
        } else {
            0
        }

        _progress.postValue(progress)
    }

    fun startUpdates() {
        handler.post(updateRunnable)
    }

    fun stopUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onCleared() {
        stopUpdates()
        super.onCleared()
    }
}






class LegacyLiveData<T>: ViewModel{

    private var _value:MutableLiveData<T> = MutableLiveData<T>()

    val value: LiveData<T>
        get() {
            return _value
        }

    constructor(initialValue:T):super(){
        _value.value=initialValue
    }
    val handler = Handler(Looper.getMainLooper())

   public fun Update(newValue:T){
        _value.value=newValue
    }


}