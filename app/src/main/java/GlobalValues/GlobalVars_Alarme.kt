package GlobalValues

import DataClasses_Ojects.Alarma_Item
import android.os.Parcelable
import java.time.LocalDateTime

var editingAlarm: Int = -1//which alarm i'm editing
var pleasemakethiswork:Int = 0

var doingselection: Int =
    0 // 0=default, before starting selection, 1= doing selection in proccess, 3= stop selection->0
var nrOfChecks = 0;//the 2 values for checking and unchecking allarms when editing one or more
var doingSelection2: Boolean = false

var sharedString: String = ""
var alarmDataList = ArrayList<Alarma_Item>();
var recycleState: Parcelable? = null
var verticaloffset: Int = 0

var newAllarm: Alarma_Item = Alarma_Item(SoundTime = LocalDateTime.now())

