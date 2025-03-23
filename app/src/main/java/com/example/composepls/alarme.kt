package com.example.composepls

import Adaptors.CustomRecyclerView
import Adaptors.alarmAdapter
import Classes_Ojects.alarmViewModel
import Classes_Ojects.liveNextAlarm
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composepls.ora_am_or_pm
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import org.w3c.dom.Text
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


var editingAlarm:Int=-1//which alarm i'm editing


var doingselection:Int =0 // 0=default, before starting selection, 1= doing selection in proccess, 3= stop selection->0
var nrOfChecks=0;//the 2 values for checking and unchecking allarms when editing one or more
var doingSelection2:Boolean=false

var sharedString:String =""
var alarmDataList = ArrayList<alarmViewModel>();
var recycleState: Parcelable? =null
var verticaloffset:Int = 0

var newAllarm: alarmViewModel = alarmViewModel(SoundTime = LocalDateTime.now())

open class alarme : Fragment(R.layout.fragment_alarme), alarmAdapter.OnSwitchListener, alarmAdapter.onCardClickListener,
    alarmAdapter.onCardLongPressListener {





    lateinit var sf: SharedPreferences;
    lateinit var editor : SharedPreferences.Editor;


    private lateinit var nextAlarm:TextView
    private lateinit var exactTimeofIt:TextView
    private lateinit var recycleview: CustomRecyclerView
    private lateinit var fablayout:ConstraintLayout
    private lateinit var appbar: AppBarLayout
    private lateinit var coordinator: CoordinatorLayout

    val livenextalarm:liveNextAlarm by viewModels()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val displayList:ArrayList<alarmViewModel> =ArrayList<alarmViewModel>()
        displayList.addAll(ArrayList(alarmDataList))

        //set values here
        val localContext = requireContext();
        val plusButton: ImageView = requireView().findViewById(R.id.plusButton)
        val optionsButton: ImageView = requireView().findViewById(R.id.optionsButton)
        nextAlarm = requireView().findViewById(R.id.nextAllarm)
        exactTimeofIt = requireView().findViewById(R.id.whatexacttime)
        recycleview = requireView().findViewById(R.id.mylist);
        recycleview.layoutManager = LinearLayoutManager(requireContext());

        sf = localContext.getSharedPreferences("My SF", MODE_PRIVATE);
        editor = sf.edit();

        fablayout =requireView().findViewById(R.id.FABlayout)
        val fabON: FloatingActionButton = requireView().findViewById(R.id.turnOnAll)
        val fabOFF: FloatingActionButton = requireView().findViewById(R.id.turnOffAll)
        val fabDelete: FloatingActionButton = requireView().findViewById(R.id.delete)
        appbar = requireView().findViewById(R.id.appbar)
        coordinator = requireView().findViewById<CoordinatorLayout>(R.id.coordinatorlayout)


        //defaults
        doingselection=0
        for(i in 0..<alarmDataList.size)
        {
            alarmDataList[i].editChecker=false
        }
        nrOfChecks=0
        recycleview.adapter?.notifyDataSetChanged()
        requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
        exactTimeofIt.text=""
        //defaults


        //live data example here
        livenextalarm.next_alarm_in.observe(viewLifecycleOwner){
                alarmtime->

            val timewhen=update_main_text(requireView())
            val formated_time= NextAlarmTextSet(requireView(),timewhen)

            nextAlarm.text=formated_time
            val formatter = DateTimeFormatter.ofPattern("HH:mm\ndd/MM/yyyy")
            val formattedDate = timewhen.format(formatter)

            if(timewhen.isAfter(LocalDateTime.now()))
                exactTimeofIt.text="${formattedDate}"
        }



        //back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back press
            if (nrOfChecks>0 || doingselection==1) {
                doingselection = 2
                nrOfChecks = 0
                recycleview.adapter?.notifyDataSetChanged()
                setFab(false)

                requireView().findViewById<TextView>(R.id.textAlarms).visibility=View.VISIBLE
                requireView().findViewById<TextView>(R.id.Alltext).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
                requireView().findViewById<ConstraintLayout>(R.id.clickAll).isClickable=false


            } else {
                // Allow the system to handle the back press
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }


        plusButton.setOnClickListener{
            doingselection=0
            nrOfChecks=0
            setFab(false)
            val intent = Intent(requireContext(), addAlarmActivity::class.java)
            startActivity(intent);
        }
        optionsButton.setOnClickListener{
            doingselection=0
            nrOfChecks=0
            setFab(false)

            //TO DO
        }
        fabDelete.setOnClickListener{

            if(doingselection==1 && nrOfChecks>0)
            {
                for(i in alarmDataList.size-1 downTo 0)
                {

                    if(alarmDataList[i].editChecker)
                    {
                        alarmDataList.removeAt(i)

                        //canceling allarm function HEREEEEEEEEEEEEEEEE
                    }

                }
                nrOfChecks=0
                doingselection=2
                recycleview.adapter?.notifyDataSetChanged();update_aux_text()


            }
            setFab(false)

            requireView().findViewById<TextView>(R.id.textAlarms).visibility=View.VISIBLE
            requireView().findViewById<TextView>(R.id.Alltext).visibility=View.INVISIBLE
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).visibility=View.INVISIBLE
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
            requireView().findViewById<ConstraintLayout>(R.id.clickAll).isClickable=false


        }
        fabON.setOnClickListener{
            if(nrOfChecks>0)
            {
                for(i in alarmDataList.size-1 downTo 0)
                {
                    if(alarmDataList[i].editChecker)
                        alarmDataList[i].active= true
                }
                setFab(false)
                nrOfChecks=0
                doingselection=2
                recycleview.adapter?.notifyDataSetChanged()
            }
        }
        fabOFF.setOnClickListener{
            if(nrOfChecks>0)
            {
                for(i in alarmDataList.size-1 downTo 0)
                {
                    if(alarmDataList[i].editChecker) {
                        alarmDataList[i].active = false
                    }
                }
                setFab(false)
                nrOfChecks=0
                doingselection=2
                recycleview.adapter?.notifyDataSetChanged()
            }
        }


        // all button
        requireView().findViewById<ConstraintLayout>(R.id.clickAll).setOnClickListener{
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=!requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked
            nrOfChecks=0
            for(i in 0..<alarmDataList.size)
            {
                alarmDataList[i].editChecker=requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked
                nrOfChecks+= alarmDataList[i].editChecker.toInt()
            }
            recycleview.adapter?.notifyDataSetChanged()
        }




        val swiperefreshlayout:SwipeRefreshLayout=requireView().findViewById(R.id.SwipeRefresh)

        swiperefreshlayout.setOnRefreshListener {
            displayList.clear()
            displayList.addAll(ArrayList(alarmDataList))
            alarmDataList.clear()
            alarmDataList.addAll(ArrayList(displayList))

            recycleview.adapter!!.notifyDataSetChanged()

            swiperefreshlayout.isRefreshing=false


        }

    }


    fun setFab(visibility:Boolean){
        if(visibility) {
            fablayout.visibility = ViewGroup.VISIBLE
            //add animations here
        }
        else {
            fablayout.visibility = ViewGroup.INVISIBLE
            // add animations here
        }
        update_main_text(requireView())
    }

    override fun onCardLongPress(position: Int, view: alarmAdapter.ItemViewHolder) {



        if (alarmDataList[position].editChecker) {
            alarmDataList[position].editChecker = false
            nrOfChecks--
        }
        else {
            alarmDataList[position].editChecker = true
            nrOfChecks++
        }

        if(nrOfChecks== alarmDataList.size)
        {
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=true
        }
        else
        {
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
        }


        //recycleview.adapter?.notifyDataSetChanged()


        if (doingselection == 0 || doingselection==2) {
            if (nrOfChecks > 0) {
                // incep selectia
                requireView().findViewById<TextView>(R.id.textAlarms).visibility=View.INVISIBLE
                requireView().findViewById<TextView>(R.id.Alltext).visibility=View.VISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).visibility=View.VISIBLE
                requireView().findViewById<ConstraintLayout>(R.id.clickAll).isClickable=true
                // Transition from doingselection == 0 to doingselection == 1
                setFab(true) // Enable FAB
                doingselection = 1


                recycleview.adapter?.notifyDataSetChanged()


            }
            else
            {

            }
        } else if (doingselection == 1) {
            if (nrOfChecks > 0) {
                // Update the specific item that changed
                setFab(true) // Keep FAB enabled
                doingSelection2=true
                recycleview.adapter?.notifyItemChanged(position)

            } else {
                // termin selectia
                requireView().findViewById<TextView>(R.id.textAlarms).visibility=View.VISIBLE
                requireView().findViewById<TextView>(R.id.Alltext).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
                requireView().findViewById<ConstraintLayout>(R.id.clickAll).isClickable=false
                // No checks, retract and reset to doingselection == 0
                setFab(false) // Disable FAB
                doingselection = 2 // Intermediate state
                recycleview.adapter?.notifyDataSetChanged()


                doingSelection2=false
            }
        }


        // setarea culorii din afara adaptorului, este posibil!
        //view.card.background= ContextCompat.getDrawable(view.itemView.context, R.color.blue)
        //recycleview.adapter?.notifyItemChanged(position)



    }

    override fun oncardClick(position: Int, view: alarmAdapter.ItemViewHolder) {


        if(doingselection==1) {
            if (alarmDataList[position].editChecker) {
                alarmDataList[position].editChecker = false
                nrOfChecks--
            }
            else {
                alarmDataList[position].editChecker = true
                nrOfChecks++
            }
        }

        if(nrOfChecks== alarmDataList.size)
        {
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=true
        }
        else
        {
            requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
        }




        if (doingselection == 0 || doingselection==2) {
            if (nrOfChecks > 0) {
                // Transition from doingselection == 0 to doingselection == 1
                setFab(true) // Enable FAB
                doingselection = 1
                recycleview.adapter?.notifyDataSetChanged()
            }
            else
            {
                editingAlarm=position
                newAllarm = alarmDataList[position]

                val intent = Intent(requireContext(),addAlarmActivity::class.java)
                startActivity(intent);

            }
        } else if (doingselection == 1) {
            if (nrOfChecks > 0) {
                // Update the specific item that changed
                setFab(true) // Keep FAB enabled
                doingSelection2=true
                recycleview.adapter?.notifyItemChanged(position)
            } else {
                // termin selectia
                requireView().findViewById<TextView>(R.id.textAlarms).visibility=View.VISIBLE
                requireView().findViewById<TextView>(R.id.Alltext).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).visibility=View.INVISIBLE
                requireView().findViewById<MaterialCheckBox>(R.id.allcheck).isChecked=false
                requireView().findViewById<ConstraintLayout>(R.id.clickAll).isClickable=false


                // No checks, retract and reset to doingselection == 0
                setFab(false) // Disable FAB
                doingselection = 2 // Intermediate state
                doingSelection2=false
                recycleview.adapter?.notifyDataSetChanged()

            }
        }






    }

    // switch from active to inactive allarm
    override fun onSwitchPress(position: Int,view: alarmAdapter.ItemViewHolder) {
        val bol:Boolean = view.swich.isChecked
        if (bol ==true){
            view.timp.setTextColor(requireContext().getColor(R.color.white))
            view.am.setTextColor(requireContext().getColor(R.color.white))

        }
        else if(bol==false)
        {
            view.timp.setTextColor(requireContext().getColor(R.color.inactive))
            view.am.setTextColor(requireContext().getColor(R.color.inactive))

        }
        alarmDataList[position].active=bol


        // update the big text above
        val timewhen=update_main_text(requireView())
        val formated_time= NextAlarmTextSet(requireView(),timewhen)

        nextAlarm.text=formated_time
        val formatter = DateTimeFormatter.ofPattern("HH:mm\ndd/MM/yyyy")
        val formattedDate = timewhen.format(formatter)

        if(timewhen.isAfter(LocalDateTime.now()))
            exactTimeofIt.text="${formattedDate}"
    }



    override fun onPause() {
        super.onPause()



        //saves data to alarm_list.json
        saveAsJson(requireContext(),"alarm_list", alarmDataList)



        //saves the state of the recycleview and collapsing appbar
        recycleState= recycleview.layoutManager?.onSaveInstanceState()
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            verticaloffset = verticalOffset
        })









        if(R.string.page_number ==0)
        {
            nrOfChecks=0
            doingselection=2
            recycleview.adapter?.notifyDataSetChanged()
        }

    }

    override fun onResume() {
        super.onResume()




        // restores the state of the recyclerview and the collapsing appbar
        recycleview.layoutManager?.onRestoreInstanceState(recycleState)
        appbar.post {
            val params = appbar.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            behavior.setTopAndBottomOffset(verticaloffset) // Collapse fully
        }




        //loads data from alarm_list.json
        alarmDataList= loadFromJson(requireContext(),"alarm_list", alarmDataList)

        //updates the list if an alarm has been added
        checkReturnFromAddAlarm()

        for(i in alarmDataList)
        {
            i.editChecker=false
        }

        //set the time for main text

        update_main_text(requireView())





        //how to add data to the memory list
        //data.add(alarmViewModel(2,15,"PM",true,type = ArrayList<Int>(1).apply{add(1)},ArrayList<String>(1).apply{add("Andrei")}))

        //how to update the list:
        newAllarm.ora=0
        newAllarm.type[11]=-1
        val adaptor = alarmAdapter(alarmDataList, this, this,this);

        recycleview.adapter = adaptor;


        //sorts the list
        alarmDataList.sortWith(compareBy<alarmViewModel> { ora_am_or_pm(it)}.thenBy { it.minute })
        recycleview.adapter?.notifyDataSetChanged()











        //updates only sharedstring to display the data
        update_aux_text()
    }

    fun checkReturnFromAddAlarm() {
        if (newAllarm.type[11] > 0) //daca am revenit din add_new_alarm
        {


            if (editingAlarm < 0)//daca se adauga intr-adevar o alarma
            {
                alarmDataList.add(
                    alarmViewModel(
                        newAllarm.ora,
                        newAllarm.minute,
                        newAllarm.aM,
                        true,
                        newAllarm.type,
                        newAllarm.properties,
                        false,
                        newAllarm.SoundTime
                    )
                )
                saveAsJson(requireContext(),"alarm_list", alarmDataList)
            } else // s-a editat o alarma
            {
                if (editingAlarm >= 0) {
                    alarmDataList[editingAlarm].ora = newAllarm.ora
                    alarmDataList[editingAlarm].minute = newAllarm.minute
                    alarmDataList[editingAlarm].aM = newAllarm.aM
                    alarmDataList[editingAlarm].active = newAllarm.active
                    alarmDataList[editingAlarm].type = newAllarm.type
                    alarmDataList[editingAlarm].properties = newAllarm.properties
                    alarmDataList[editingAlarm].editChecker = newAllarm.editChecker
                    alarmDataList[editingAlarm].SoundTime = newAllarm.SoundTime


                    recycleview.adapter?.notifyItemChanged(editingAlarm)

                    saveAsJson(requireContext(), "alarm_list", alarmDataList)
                }

            }
        }

        editingAlarm = -1
    }


}




fun setAllarms(){

}



fun NextAlarmTextSet(view:View,incoming_alarm_in:LocalDateTime):String{
    val nextalarm:TextView = view.findViewById(R.id.nextAllarm)
    val minitextview:TextView=view.findViewById(R.id.whatexacttime)
    val right_now=LocalDateTime.now()
    var setTextPlease=1

    if(LocalDateTime.now().isAfter(incoming_alarm_in))
        setTextPlease=0

    var endtext:String="No alarm set."

    if(setTextPlease==0)
    {

        minitextview.text=""
    }
    else
    {

        val day=ChronoUnit.DAYS.between(right_now,incoming_alarm_in)
        val hours=ChronoUnit.HOURS.between(right_now,incoming_alarm_in)%24
        val minutes=ChronoUnit.MINUTES.between(right_now,incoming_alarm_in)%60

        var nextalarmtext="Alarm will sound in ";

        var track=0;
        if(day>0){nextalarmtext+="$day day";track++}
        if(day>1){nextalarmtext+="s"}
        if(track>0){
            nextalarmtext+=", "
        }
        if(hours>0){nextalarmtext+="$hours hour";track++}
        if(hours>1){nextalarmtext+="s"}
        if(track>0){
            nextalarmtext+=" and "
        }
        if(minutes>0){nextalarmtext+="$minutes minute";track++}
        if(minutes>1){nextalarmtext+="s"}

        nextalarm.text=nextalarmtext
        endtext=nextalarmtext




    }
    return endtext
}

fun update_main_text(view:View):LocalDateTime{
    val nextAlarm:TextView=view.findViewById(R.id.nextAllarm)
    val right_now:LocalDateTime= LocalDateTime.now()
    var incoming_alarm_in =right_now.minusDays(1)
    val minitextview:TextView=view.findViewById(R.id.whatexacttime)

    var nrOfPorniteAlarme=0

    // this refreshes allarms's next sound
    if (alarmDataList.size > 0) {
        incoming_alarm_in =right_now.plusDays(365)
        for (i in alarmDataList.size - 1 downTo 0) {
            alarmDataList[i].SoundTime =
                assign_time(alarmDataList[i], alarmDataList[i].type[0] > 10, false)

            Log.i("MYTAG", "Alarm ${i}: ${alarmDataList[i].SoundTime}")

            if (alarmDataList[i].active && alarmDataList[i].SoundTime.isBefore(incoming_alarm_in)) {
                incoming_alarm_in = alarmDataList[i].SoundTime
                nrOfPorniteAlarme+=1
            }
        }
    }
    if(nrOfPorniteAlarme==0)
    {
        incoming_alarm_in=LocalDateTime.now()
    }


    //from here on out is the simple setting of the text
    NextAlarmTextSet(view,incoming_alarm_in)

    return incoming_alarm_in

}



fun update_aux_text(){
    if(alarmDataList.size>0) {


        var auxString: String = ""


        for (i in 0..<alarmDataList.size) {

            auxString+="${alarmDataList[i].SoundTime.hour}:${alarmDataList[i].SoundTime.minute}   ${alarmDataList[i].type[0].toString()}     ${alarmDataList[i].type.slice(8..10).joinToString("/")}    ${alarmDataList[i].SoundTime.toLocalDate()}  ${LocalDateTime.now().hour}\n"
            auxString+="${alarmDataList[i].type.slice(1..7).joinToString(".")}      ${alarmDataList[i].SoundTime}\n"
        }


        sharedString = auxString
    }
}




fun saveAsJson(context: Context, filename:String, data:Any) {
    val json=Gson().toJson(data);
    val filepath=context.filesDir.toString()+"/"+filename+".json"

    File(filepath).writeText(json)
}

inline fun <reified T> loadFromJson(context:Context,filename: String,data: T): T {

    val filepath=context.filesDir.toString()+"/"+filename+".json"
    val file=File(filepath)

    if(file.exists()) {
        val loadedFile = file.readText()
        val type = object:TypeToken<T>(){}.type
        val readData:T = Gson().fromJson(loadedFile, type)

        return readData
    }
    return data
}