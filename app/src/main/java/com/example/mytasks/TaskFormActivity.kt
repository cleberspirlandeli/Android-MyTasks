package com.example.mytasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_task_form.*
import java.text.SimpleDateFormat
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val items = resources.getStringArray(R.array.priorities_array)
        val adapter = ArrayAdapter(baseContext, R.layout.option_item_priority, items)

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
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.swtComplete -> changeImage()
            R.id.btnDate -> openDatePicker()
            R.id.autoCompletePriority -> changeImageAutoComplete()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }
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