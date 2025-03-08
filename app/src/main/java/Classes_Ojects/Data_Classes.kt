package Classes_Ojects


import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.Serializable
import java.time.LocalDateTime


data class alarmViewModel(
    var ora: Int = 0,
    var minute: Int = 0,
    var aM: String = "AM",
    var active: Boolean = true,
    var type: ArrayList<Int> = ArrayList<Int>(15).apply { repeat(15) { add(0) }; this[8]=LocalDateTime.now().dayOfMonth;this[9]=LocalDateTime.now().month.value;this[10]=LocalDateTime.now().year },
    var properties: ArrayList<String> = ArrayList<String>(5).apply { repeat(5) { add("") } },
    var editChecker: Boolean = false,
    var SoundTime: LocalDateTime // This is the only required parameter
) : Serializable


class liveNextAlarm:ViewModel(){

    private val _next_alarm = MutableLiveData<LocalDateTime>()

    val next_alarm_in:LiveData<LocalDateTime>
        get()=_next_alarm

    fun updateAlarmTime(newtime:LocalDateTime){
        _next_alarm.value=newtime
    }

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            // Update the alarm time every second
            _next_alarm.value = LocalDateTime.now()
            handler.postDelayed(this, 1000)
        }
    }

    init {
        // Start the periodic updates
        handler.post(runnable)
    }

    override fun onCleared() {
        super.onCleared()
        // Stop the Handler when the ViewModel is cleared
        handler.removeCallbacks(runnable)
    }
}

//<x x x x x x x x x x x...>
//
//1-one time,single item vector 								{size 1}
//2-weekly, check the next 7 values for 0 or 1(7 days of the week)			{size 8}
//3-everyday,single item vector								{size 1}
//
//10+(1..3)
//
//if(val>10) => delayed allarm, look at values 5,6,7 to see when it should restart(calender again)
//
//
//
//properties:
//
//Title



//for properties
// stuff like if it has alarm sound and which sound from where in the storage
// if it has vibration and what kind
//how long and how many times it should "snooze"
//