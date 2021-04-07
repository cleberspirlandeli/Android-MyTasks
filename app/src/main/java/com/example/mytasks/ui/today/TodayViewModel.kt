package com.example.mytasks.ui.today

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytasks.R
import com.example.mytasks.TaskFormActivity
import com.example.mytasks.common.Receiver
import com.example.mytasks.common.constants.ScreenFilterConstants
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.listener.ValidationListener
import com.example.mytasks.service.model.GenericModel
import com.example.mytasks.service.model.TaskModel
import com.example.mytasks.service.repository.TaskRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext


    private val auth = Firebase.auth
    private val user = auth.currentUser

    // OBSERVERS
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _validation = MutableLiveData<ValidationListener>()
    val validation: LiveData<ValidationListener> = _validation

    private val _listTasks = MutableLiveData<List<TaskModel>?>()
    val listTasks: LiveData<List<TaskModel>?> = _listTasks

    private val _task = MutableLiveData<TaskModel>()
    val task: LiveData<TaskModel> = _task

    // REPOSITORY
    private val mTaskRepository: TaskRepository = TaskRepository(application)

//    private lateinit var alarmManager: AlarmManager

    private val mApiCallbackListener = object : ApiCallbackListener<List<TaskModel>> {

        override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
            _listTasks.value = result
            setLoading()
        }

        override fun onFailure(message: String) {
            _listTasks.value = null
            setLoading()
        }
    }

    fun delete(task: TaskModel, screen: Int) {

        task.image.let {
            if (!it.isNullOrEmpty()) {
                deletePhoto(it)
            }
        }

        task.id?.let { deleteTask(it, screen) }

        task.notificationId?.let { it -> deleteNotification(it) }
    }

    private fun deleteNotification(notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_ONE_SHOT)

        alarmManager.cancel(pendingIntent)
    }

    private fun deleteTask(id: String, screen: Int) {
        val listener = object : ApiCallbackListener<String> {
            override fun onSuccess(result: String?, statusCode: Int?) {
                getListByTypeScreen(screen)
                _validation.value =
                    ValidationListener(successMessage = context.getString(R.string.task_removed_successfully))
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
            }
        }

        mTaskRepository.deleteTask(id, listener)
    }

    private fun getListByTypeScreen(screen: Int) {
        when (screen) {
            ScreenFilterConstants.SCREEN.TODAY -> getListAllTasks()
            ScreenFilterConstants.SCREEN.THIS_WEEK -> getListThisWeekTasks()
            ScreenFilterConstants.SCREEN.NEXT_WEEK -> getListNextWeekTasks()
            ScreenFilterConstants.SCREEN.DONE -> getListDoneTasks()
            ScreenFilterConstants.SCREEN.ALL_UNDO -> getListNotCompletedTasks()
        }
    }

    fun deletePhoto(imageUrl: String) {
        val listener = object : ApiCallbackListener<String> {

            override fun onSuccess(result: String?, statusCode: Int?) {
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
            }
        }

        mTaskRepository.deletePhoto(imageUrl, listener)
    }

    private fun getStartOfDay(): Calendar {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 0, 0, 0)
        return calendar
    }

    private fun getEndOfDay(): Calendar {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 23, 59, 59)
        return calendar
    }

    private fun getStartAndEndDayOfWeek(): GenericModel {
        val calendar = Calendar.getInstance()
        val calendarStart = getStartOfDay()
        val calendarEnd = getEndOfDay()

        val day = calendar.get(Calendar.DAY_OF_WEEK)

        when (day) {
            // SEGUNDA
            Calendar.MONDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -1)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 5)
            }

            // TERÇA-FEIRA
            Calendar.TUESDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -2)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 4)
            }

            // QUARTA-FEIRA
            Calendar.WEDNESDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -3)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 3)
            }

            // QUINTA-FEIRA
            Calendar.THURSDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -4)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 2)
            }

            // SEXTA-FEIRA
            Calendar.FRIDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -5)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 2)
            }

            // SABADO-FEIRA
            Calendar.SATURDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, -6)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 1)
            }

            // DOMINGO-FEIRA
            Calendar.SUNDAY -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 6)
            }
        }

        val obj = GenericModel().apply {
            startDate = calendarStart.timeInMillis
            endDate = calendarEnd.timeInMillis
        }

        return obj
    }

    private fun getStartAndEndDayOfNextWeek(): GenericModel {
        val calendar = Calendar.getInstance()
        val calendarStart = getStartOfDay()
        val calendarEnd = getEndOfDay()

        val day = calendar.get(Calendar.DAY_OF_WEEK)

        when (day) {
            // SEGUNDA
            Calendar.MONDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 6)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 12)
            }

            // TERÇA-FEIRA
            Calendar.TUESDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 5)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 11)
            }

            // QUARTA-FEIRA
            Calendar.WEDNESDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 4)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 10)
            }

            // QUINTA-FEIRA
            Calendar.THURSDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 3)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 9)
            }

            // SEXTA-FEIRA
            Calendar.FRIDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH,  2)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 8)
            }

            // SABADO-FEIRA
            Calendar.SATURDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 1)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 7)
            }

            // DOMINGO-FEIRA
            Calendar.SUNDAY -> {
                calendarStart.add(Calendar.DAY_OF_MONTH, 7)
                calendarEnd.add(Calendar.DAY_OF_MONTH, 13)
            }
        }

        var dayStart = calendarStart.time
        var dayEnd = calendarEnd.time

        val obj = GenericModel().apply {
            startDate = calendarStart.timeInMillis
            endDate = calendarEnd.timeInMillis
        }

        return obj
    }

    fun getListAllTasks() {
        setLoading()

        var startDay = getStartOfDay()
        var endDay = getEndOfDay()

        mTaskRepository.getListTask(user.uid, startDay.timeInMillis, endDay.timeInMillis, mApiCallbackListener)
    }

    private fun setLoading() {
        _isLoading.value = !_isLoading.value!!
    }

    fun getListDoneTasks() {
        setLoading()
        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
                setLoading()
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
                setLoading()
            }
        }

        mTaskRepository.getListByCompleteTasks(user.uid, true, listener)
    }

    fun getTaskById(task: TaskModel) {
        setLoading()

        val apiListener = object : ApiCallbackListener<TaskModel> {

          override fun onFailure(message: String) {
                _listTasks.value = null
                setLoading()
            }

            override fun onSuccess(result: TaskModel?, statusCode: Int?) {
               _task.value = result!!
                setLoading()
            }

        }

        mTaskRepository.getTaskById(task, apiListener)
    }

    fun onChangeCompleteTaskClick(id: String, statusTask: Boolean, screen: Int) {
        setLoading()
        val listener = object : ApiCallbackListener<String> {
            override fun onSuccess(result: String?, statusCode: Int?) {
                getListByTypeScreen(screen)
                _validation.value =
                    ValidationListener(successMessage = context.getString(R.string.task_updated_successfully))
                setLoading()
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
                setLoading()
            }
        }

        mTaskRepository.onChangeCompleteTaskClick(id, statusTask, listener)
    }

    fun getListThisWeekTasks() {
        setLoading()
        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
                setLoading()
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
                setLoading()
            }
        }

        val filters = getStartAndEndDayOfWeek()
        mTaskRepository.getListWeekByFilterTasks(user.uid, filters, listener)
    }

    fun getListNextWeekTasks() {
        setLoading()
        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
                setLoading()
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
                setLoading()
            }
        }

        val filters = getStartAndEndDayOfNextWeek()
        mTaskRepository.getListWeekByFilterTasks(user.uid, filters, listener)
    }

    fun getListNotCompletedTasks() {
        setLoading()
        mTaskRepository.getListByCompleteTasks(user.uid, false, mApiCallbackListener)
    }
}


