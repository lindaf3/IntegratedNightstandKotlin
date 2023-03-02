package com.example.myapplication

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.datepicker.MaterialDatePicker

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DatetimePickerFragment(recent: LocalDateTime, private val title: String = "Select Datetime:"): DialogFragment() {

    var datetime = recent
    @RequiresApi(Build.VERSION_CODES.O)
    var hour = recent.hour
    @RequiresApi(Build.VERSION_CODES.O)
    var minute = recent.minute
    @RequiresApi(Build.VERSION_CODES.O)
    var date = recent.toLocalDate()
    var dateText =  UNINITIALIZED_DATE
    var undo = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, container, false)
        val setTime: Button = view.findViewById(R.id.timeSelector)
        val setDate: Button = view.findViewById(R.id.dateSelector)
        val set: Button = view.findViewById(R.id.datetimePickerSet)
        val cancel: Button = view.findViewById(R.id.datetimePickerCancel)
        val heading: TextView = view.findViewById(R.id.datetimePickerHeading)

        heading.text = title

        fun revert(){
            setTime.text = SELECT_TIME
            setDate.text = SELECT_DATE
            dateText = UNINITIALIZED_DATE
            hour = UNINITIALIZED_TIME
            minute = UNINITIALIZED_TIME
        }

        setTime.setOnClickListener{
            val timePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText(SELECT_TIME)
                .setHour(datetime.hour)
                .setMinute(datetime.minute)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            timePicker.show(requireActivity().supportFragmentManager, "data time")
            timePicker.addOnPositiveButtonClickListener {
                hour = timePicker.hour
                minute = timePicker.minute
                AlarmClock.setAlarm(timePicker, setTime, SELECT_TIME)
            }

        }

        setDate.setOnClickListener{
            val datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Session Date")
                .build()
            datePicker.show(requireActivity().supportFragmentManager, "data date")
            datePicker.addOnPositiveButtonClickListener {
                dateText = datePicker.headerText
                Date.setDate(dateText, setDate)
                date = Date.parseDate(dateText)
            }
        }

        cancel.setOnClickListener{
            revert()
            undo = true
            dismiss()
        }


        set.setOnClickListener{
            undo = false
            if(validData()){
                val time = LocalTime.of(hour, minute)
                datetime = LocalDateTime.of(date, time)
            }
            else{
                revert()
            }
            dismiss()
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDismiss(dialog: DialogInterface) {
        val bundle = Bundle()
        bundle.putBoolean("cancel", undo)
        if(!undo){
            val valid = validData()
            bundle.putBoolean("use?", valid)
            if(valid){
                bundle.putString("datetimeText", datetime.format(DateTimeFormatter.ISO_DATE_TIME))
            }
        }



        setFragmentResult("requestKey", bundle)
        super.onDismiss(dialog)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validData(): Boolean{
        return (dateText != UNINITIALIZED_DATE && hour != UNINITIALIZED_TIME && minute != UNINITIALIZED_TIME)
    }




    companion object {
        private const val SELECT_DATE = "Select Date"
        private const val SELECT_TIME = "Select TIME"
        private const val UNINITIALIZED_DATE = "INVALID"
        private const val UNINITIALIZED_TIME = -1


    }
}

object Datetime{
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun setDatetime(datetime: LocalDateTime, btn: Button) {
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        btn.text = datetime.format(formatter) + " " +AlarmClock.getAlarmTime(datetime.hour, datetime.minute,"INVALID TIME");
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun datetimeToDouble(startDatetime: LocalDateTime?, endDatetime: LocalDateTime?): Double{
        return ChronoUnit.MINUTES.between(startDatetime, endDatetime)/60.0
    }
}