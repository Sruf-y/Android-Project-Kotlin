package Classes_Ojects


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