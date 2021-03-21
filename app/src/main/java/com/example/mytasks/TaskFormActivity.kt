package com.example.mytasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mytasks.model.TaskModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TaskFormActivity : AppCompatActivity(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    private val mDateFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    lateinit var db: FirebaseFirestore
    private var items = arrayOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

        db = Firebase.firestore

        // Set make default value
        autoCompletePriority.setText(adapter.getItem(0).toString(), false)

        autoCompletePriority.setAdapter(adapter);

        listeners()
        setActualDateAndHours()
    }

    private fun setActualDateAndHours() {
        getDateTimeCalendar()

        var dataAtual = Calendar.getInstance()
        dataAtual.add(Calendar.HOUR, 1)
        var data = dataAtual.getTime()

        var dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm");

        var hoje = dateFormat.format(data);

        btnDate.text = hoje
    }

    private fun listeners() {
        swtComplete.setOnClickListener(this)
        btnDate.setOnClickListener(this)
        autoCompletePriority.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.swtComplete -> changeImage()
            R.id.btnDate -> openDatePicker()
            R.id.autoCompletePriority -> changeImageAutoComplete()
            R.id.btnSave -> saveTask()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {
        var date = btnDate.text.toString()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val dateFormatted = date.format(formatter)

        var priorityIndex = items.indexOf(autoCompletePriority.text.toString())

        val task = hashMapOf(
            "task" to txtTask.text.toString(),
            "priority" to priorityIndex,
            "complete" to swtComplete.isChecked,
            "date" to dateFormatted,
            "description" to txtDescription.text.toString()
        )



        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d("addOnSuccessListener", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("addOnFailureListener", "Error adding document", e)
            }

        return
    }

    private fun changeImageAutoComplete() {
        Toast.makeText(baseContext, "Clicou", Toast.LENGTH_LONG).show()
    }

    private fun changeImage() {
        if (swtComplete.isChecked)
            imgTask.setImageResource(R.drawable.ic_baseline_task_alt_24)
        else
            imgTask.setImageResource(R.drawable.ic_baseline_highlight_off_24)
    }

    private fun getDateTimeCalendar() {
        val cal: Calendar = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun openDatePicker() {
        getDateTimeCalendar()

        var datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedYear = year
        savedMonth = month
        savedDay = dayOfMonth

        getDateTimeCalendar()
        TimePickerDialog(this, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        var date =
            "${formatNumber(savedDay)}/${formatNumber(savedMonth + 1)}/${savedYear} ${formatNumber(
                savedHour
            )}:${formatNumber(savedMinute)}"

        btnDate.text = date
    }

    private fun formatNumber(number: Int): String {
        return if (number == 0) "00" else if (number < 10) "0${number}" else "${number}"
    }

}