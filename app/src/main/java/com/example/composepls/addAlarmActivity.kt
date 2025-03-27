package com.example.composepls



import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Switch
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import Adaptors.Utils.Companion.dP
import Classes_Ojects.alarmViewModel
import Functions.CustomSnack
import Functions.HideKeyboard
import android.content.res.ColorStateList
import android.content.res.Resources
import android.service.autofill.OnClickAction
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.compose.material3.Snackbar
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.graphics.translationMatrix
import androidx.core.widget.NestedScrollView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit



class addAlarmActivity : AppCompatActivity() {
    lateinit var hourPicker:NumberPicker
    lateinit var minutePicker:NumberPicker
    lateinit var amPicker:NumberPicker
    lateinit var Luni:MaterialCheckBox
    lateinit var Marti:MaterialCheckBox
    lateinit var Miercuri:MaterialCheckBox
    lateinit var Joi:MaterialCheckBox
    lateinit var Vineri:MaterialCheckBox
    lateinit var Sambata:MaterialCheckBox
    lateinit var Duminica:MaterialCheckBox

    lateinit var calendar_background:ConstraintLayout
    lateinit var calendar: CalendarView
    lateinit var calendar_click: ImageView

    lateinit var saveButton:Button
    lateinit var cancelButton:Button
    lateinit var textedit: EditText

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_add_alarm)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val wherebuttonsarea = findViewById<LinearLayout>(R.id.whereButtonsAre)

        //setBarToSwipeUp(window)
        //adjustViewsForKeyboard(wherebuttonsarea)

//        val composeview:ComposeView = findViewById(R.id.thing2inscroller)
//
//        composeview.setContent {
//            HideKeyboard()
//        }




        cancelButton=findViewById(R.id.button3)
        saveButton=findViewById(R.id.button5)
        hourPicker=findViewById(R.id.numberPicker)
        minutePicker=findViewById(R.id.numberPicker2)
        amPicker=findViewById(R.id.numberPicker3)
        Luni=findViewById(R.id.lunea)
        Marti=findViewById(R.id.marti)
        Miercuri=findViewById(R.id.miercuri)
        Joi=findViewById(R.id.joi)
        Vineri=findViewById(R.id.vineri)
        Sambata=findViewById(R.id.sambata)
        Duminica=findViewById(R.id.duminica)
        textedit= findViewById(R.id.EditAlarmTitle)
        calendar=findViewById(R.id.calendarView)
        calendar_background=findViewById(R.id.calend_back)
        calendar_click=findViewById(R.id.calendarClick)


//        val auxbutton:Button = findViewById(R.id.newbuttondeletelater)
//        auxbutton.setOnClickListener{
//            val intent= Intent(this,AlarmActivity::class.java)
//            startActivity(intent)
//        }
        var selected_a_new_day=-1
        newAllarm.type[11]=-1

        newAllarm.SoundTime=LocalDateTime.now()



        textedit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Clear focus and hide the keyboard
                textedit.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textedit.windowToken, 0)
                true // Consume the event
            } else {
                false // Let the system handle other actions
            }
        }





        hourPicker.minValue=0
        hourPicker.maxValue=11
        val displayedValues1 = Array(12) { i -> if (i == 0) "12" else i.toString() }
        hourPicker.displayedValues=displayedValues1
        minutePicker.minValue=0
        minutePicker.maxValue=59
        val displayedValues = Array(60) { i -> String.format("%02d", i) }
        minutePicker.displayedValues = displayedValues
        amPicker.minValue=0;
        amPicker.maxValue=1;
        amPicker.displayedValues= arrayOf("AM","PM");


        //setari valori initiale
        if(editingAlarm==-1) //not editing an allarm so it's making a new one
        {
            hourPicker.value=6
            minutePicker.value=0
            amPicker.value=0

            // delay off
            calendar_click.backgroundTintList= ColorStateList.valueOf(getColor(R.color.liftedBackgrounds))





        }
        else {
            // daca editez o alarma care exista deja, i preiau valorile

            textedit.setText(newAllarm.properties[0])

            when(newAllarm.type[0]%10){
                1->{
                    //need to make it so some text displays when the allarm will sound, otherwise if nothing was selected in calendar, next time the date comes

                }
                2->{

                    Luni.isChecked = newAllarm.type[1].toBool()
                    Marti.isChecked = newAllarm.type[2].toBool()
                    Miercuri.isChecked = newAllarm.type[3].toBool()
                    Joi.isChecked = newAllarm.type[4].toBool()
                    Vineri.isChecked = newAllarm.type[5].toBool()
                    Sambata.isChecked = newAllarm.type[6].toBool()
                    Duminica.isChecked = newAllarm.type[7].toBool()



                }
                3->{
                    Luni.isChecked=true
                    Marti.isChecked=true
                    Miercuri.isChecked=true
                    Joi.isChecked=true
                    Vineri.isChecked=true
                    Sambata.isChecked=true
                    Duminica.isChecked=true


                }

            }

            // load the delay if there is any
            if(newAllarm.type[0]>10){




                calendar.minDate=Calendar.getInstance().timeInMillis
                val cal_aux=Calendar.getInstance()
                cal_aux.set(newAllarm.SoundTime.year,newAllarm.SoundTime.monthValue-1, newAllarm.SoundTime.dayOfMonth)
                calendar.date = cal_aux.timeInMillis

                calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.activated))
            }
            else{ // alarma fara delay
                ColorStateList.valueOf(getColor(R.color.liftedBackgrounds))
            }


            hourPicker.value = newAllarm.ora
            minutePicker.value= newAllarm.minute
            amPicker.value= if(newAllarm.aM=="AM") 0 else 1
        }

        checkZileleSaptamanii()
        newAllarm.SoundTime= assign_time(newAllarm,calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)),true)

        cancelButton.setOnClickListener{
            newAllarm.type[11]=-1
            checkZileleSaptamanii()
            newAllarm.SoundTime=assign_time(newAllarm,calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)),true)
            finish()
        }

        saveButton.setOnClickListener{

            var checknumber=0
            newAllarm.type[11]=1
            newAllarm.ora= hourPicker.value
            newAllarm.minute= minutePicker.value
            if(amPicker.value==0)
                newAllarm.aM="AM"
            else
                newAllarm.aM="PM"

            if(Luni.isChecked){checknumber++}
            if(Marti.isChecked){checknumber++}
            if(Miercuri.isChecked){checknumber++}
            if(Joi.isChecked){checknumber++}
            if(Vineri.isChecked){checknumber++}
            if(Sambata.isChecked){checknumber++}
            if(Duminica.isChecked){checknumber++}

            if(checknumber==7) //everyday
            {
                newAllarm.type[0]=3
            }
            else if(checknumber==0) // one time only
            {
                newAllarm.type[0]=1
            }
            else // chosen days
            {
                newAllarm.type[0] = 2

                checkZileleSaptamanii()
            }

            // BOOLEAN TO INT VECTOR FOR DAYS IS WRITTEN ABOVE!!! USE IF NEEDED IN THE FUTURE, DON'T BE STUPID


            // save delay
            if(calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)))
            {
                newAllarm.type[0]+=10
            }


            if(textedit.text.toString().isNotBlank())
                newAllarm.properties[0]=textedit.text.toString()
            else
                newAllarm.properties[0]=""
            checkZileleSaptamanii()
            newAllarm.SoundTime=assign_time(newAllarm,calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)),true)


            finish()
        }

        fun Activate_CalendarClick(){
            calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.activated))

            selected_a_new_day = 0
            val tomorrow = Calendar.getInstance();




            calendar.minDate = tomorrow.timeInMillis

            if (newAllarm.type[0] > 10) {


                val cal_aux = Calendar.getInstance()
                calendar.minDate = Calendar.getInstance().timeInMillis
                cal_aux.set(
                    newAllarm.SoundTime.year,
                    newAllarm.SoundTime.month.value - 1,
                    newAllarm.SoundTime.dayOfMonth
                )
                calendar.date = cal_aux.timeInMillis

            }



        }
        fun Deactivate_CalendarClick(){
            calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.liftedBackgrounds))

            selected_a_new_day = -1

            val today = LocalDateTime.now()
            newAllarm.type[8] = today.dayOfMonth
            newAllarm.type[9] = today.monthValue
            newAllarm.type[10] = today.year

            newAllarm.ora = hourPicker.value
            newAllarm.minute = minutePicker.value
            if (amPicker.value == 0)
                newAllarm.aM = "AM"
            else
                newAllarm.aM = "PM"

            checkZileleSaptamanii()
            assign_time(
                newAllarm,
                calendar_click.backgroundTintList == ColorStateList.valueOf(getColor(R.color.liftedBackgrounds)),
                false
            )
        }

        calendar_click.setOnClickListener{

            if(calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated))) {

                calendar_background.animate()
                    .translationY(0.dP.toFloat())
                    .setDuration(300)
                    .start()
            }
            else {

                Activate_CalendarClick()

                calendar_background.animate()
                    .translationY(0.dP.toFloat())
                    .setDuration(300)
                    .start()

            }

        }
        calendar_click.setOnLongClickListener {

            if(calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated))) {

                Deactivate_CalendarClick()

                CustomSnack(calendar_click,"Delay Off")

            }


            true
        }


        calendar_background.setOnClickListener {

            calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.activated))

            calendar_background.animate()
                .translationY(800.dP.toFloat()) // Animate to this position
                .setDuration(300) // Set duration to 2 seconds
                .start()


        }

        calendar.setOnDateChangeListener{_,year,month,dayofmonth->
            newAllarm.type[8]=dayofmonth
            newAllarm.type[9]=month+1
            newAllarm.type[10]=year

            calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.activated))
            selected_a_new_day=1



        }





        onBackPressedDispatcher.addCallback() {
            // Handle the back press

            if (calendar_background.translationY == 0.dP.toFloat()) {

                val tomorrow = LocalDateTime.now()

                if(calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)))
                {
                    selected_a_new_day=1
                }
                if (selected_a_new_day == 0) {
                    newAllarm.type[8] = tomorrow.dayOfMonth
                    newAllarm.type[9] = tomorrow.monthValue
                    newAllarm.type[10] = tomorrow.year
                }

                calendar_click.backgroundTintList=ColorStateList.valueOf(getColor(R.color.activated))

                calendar_background.animate()
                    .translationY(700.dP.toFloat()) // Animate to this position
                    .setDuration(300) // Set duration to 2 seconds
                    .start()
            }
            else if(currentFocus!=null){
                //this deletes focus if anything has it
                val aux:View = currentFocus as View
                aux.clearFocus()
            }
            else{
                checkZileleSaptamanii()
                newAllarm.SoundTime=assign_time(newAllarm,calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)),true)
                // Allow the system to handle the back press
                finish()
            }
        }






    }



    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.to_up,R.anim.from_bellow)

    }

    fun checkZileleSaptamanii():Int {
        Luni=findViewById(R.id.lunea)
        Marti=findViewById(R.id.marti)
        Miercuri=findViewById(R.id.miercuri)
        Joi=findViewById(R.id.joi)
        Vineri=findViewById(R.id.vineri)
        Sambata=findViewById(R.id.sambata)
        Duminica=findViewById(R.id.duminica)

        newAllarm.type[1] = Luni.isChecked.toInt()
        newAllarm.type[2] = Marti.isChecked.toInt()
        newAllarm.type[3] = Miercuri.isChecked.toInt()
        newAllarm.type[4] = Joi.isChecked.toInt()
        newAllarm.type[5] = Vineri.isChecked.toInt()
        newAllarm.type[6] = Sambata.isChecked.toInt()
        newAllarm.type[7] = Duminica.isChecked.toInt()

        var checknumber:Int=0

        if(Luni.isChecked){checknumber++}
        if(Marti.isChecked){checknumber++}
        if(Miercuri.isChecked){checknumber++}
        if(Joi.isChecked){checknumber++}
        if(Vineri.isChecked){checknumber++}
        if(Sambata.isChecked){checknumber++}
        if(Duminica.isChecked){checknumber++}

        when(checknumber){
            0-> newAllarm.type[0]=1
            7-> newAllarm.type[0]=3
            else->newAllarm.type[0]=2
        }

        if(calendar_click.backgroundTintList==ColorStateList.valueOf(getColor(R.color.activated)))
            newAllarm.type[0]+=10

        return checknumber
    }



}





fun assign_time(data: alarmViewModel, is_delayed: Boolean, afisare_diferenta: Boolean): LocalDateTime {
    val today = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS) // Remove milliseconds


    var selected_date:LocalDateTime = LocalDateTime.of(
        data.type[10], // Year
        data.type[9], // Month
        data.type[8], // Day
        data.ora, // Hour
        data.minute, // Minute
        0 // Seconds
    ).truncatedTo(ChronoUnit.SECONDS)

    if(ora_am_or_pm(data)!=selected_date.hour)
        selected_date=selected_date.plusHours(12)

    if(is_delayed)
    {
        while(selected_date.isBefore(LocalDateTime.now()))
        {
            selected_date=selected_date.plusDays(1)
        }
    }

    if(data.type[0]%10==2){

        var counter=0;
        var i=selected_date.dayOfWeek.value
        while(counter<7)
        {

            if(data.type[i]==1)
                break;

            selected_date=selected_date.plusDays(1)
            if(i<7)
                i++
            else
                i=1
        }
    }
    else{
        if(selected_date.isBefore(LocalDateTime.now()))
            selected_date.plusDays(1)
    }

    while(selected_date.isBefore(LocalDateTime.now()))
        selected_date=selected_date.plusDays(1)
//if(afisare_diferenta)
    //Log.i("MYTAG", "Retry ${LocalTime.now()}    ${data.type.slice(1..7)}\nDifference:     Mins: $minutesDifference, Hours: $hoursDifference, Days: $daysDifference, Day ${selected_date.dayOfMonth}, Month ${selected_date.monthValue}")

    return selected_date
}


fun ora_am_or_pm(data: alarmViewModel): Int {


    if(data.aM=="AM")
        return data.ora
    else
        return data.ora+12
}

fun clickElement(givenview:View){



    // Get the current time for event timestamps
    val downTime = SystemClock.uptimeMillis()

    // Simulate touch down (ACTION_DOWN)
    val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
    givenview.dispatchTouchEvent(downEvent) // Dispatch touch down event

    // Simulate touch up (ACTION_UP) after a short delay
    val upTime = SystemClock.uptimeMillis()
    val upEvent = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, 0f, 0f, 0)
    givenview.dispatchTouchEvent(upEvent) // Dispatch touch up event


}

fun Int.toBool():Boolean{
    return if(this!=0) true else false
}

fun Boolean.toInt():Int{
    return if(this) 1 else 0
}