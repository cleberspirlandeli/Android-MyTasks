package com.example.mytasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.SimpleDateFormat
import java.util.*


class TaskFormActivity : AppCompatActivity(),
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    CompoundButton.OnCheckedChangeListener
    {

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

    lateinit var db: FirebaseFirestore
    private var items = arrayOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

        db = Firebase.firestore
        auth = Firebase.auth
        user = auth.currentUser

        // Set make default value
        autoCompletePriority.setText(adapter.getItem(0).toString(), false)
        autoCompletePriority.setAdapter(adapter);

        listeners()
        setActualDateAndHours()
    }

    private fun setActualDateAndHours() {
        getDateTimeCalendar()

        var dataAtual = Calendar.getInstance()
        dataAtual.set(Calendar.HOUR_OF_DAY, Date().hours + 1)
        var data = dataAtual.getTime()

        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm");

        var hoje = dateFormat.format(data);

        btnDate.text = hoje
    }

    private fun listeners() {
        swtComplete.setOnCheckedChangeListener(this)
        btnDate.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDate -> openDatePicker()
            R.id.btnSave -> saveTask()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {

        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = savedYear
        calendar[Calendar.MONTH] = savedMonth
        calendar[Calendar.DAY_OF_MONTH] = savedDay
        calendar[Calendar.HOUR_OF_DAY] = savedHour
        calendar[Calendar.MINUTE] = savedMinute
        calendar[Calendar.SECOND] = 0
        val timestamp: Long = calendar.timeInMillis

//        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(timestamp)
//        val currentTime = SimpleDateFormat("HH:mm").format(timestamp)

        var priorityIndex = items.indexOf(autoCompletePriority.text.toString())

        val task = hashMapOf(
            "userId" to user.uid,
            "task" to txtTask.text.toString(),
            "priority" to priorityIndex,
            "complete" to swtComplete.isChecked,
            "date" to timestamp,
            "description" to txtDescription.text.toString()
        )

        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "addOnSuccessListener",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )

                val toast =
                    Toast.makeText(this, R.string.task_successfully_created, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.view = layoutInflater.inflate(R.layout.toast_layout, null)
                toast.show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("addOnFailureListener", "Error adding document", e)
            }
    }

    private fun changeImageAutoComplete() {
        Toast.makeText(baseContext, "Clicou", Toast.LENGTH_LONG).show()
    }

    private fun getDateTimeCalendar() {
        val cal: Calendar = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
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

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.swtComplete -> {
                if (swtComplete.isChecked)
                    imgTask.setImageResource(R.drawable.ic_baseline_task_alt_24)
                else
                    imgTask.setImageResource(R.drawable.ic_baseline_highlight_off_24)
            }
        }
    }

}

