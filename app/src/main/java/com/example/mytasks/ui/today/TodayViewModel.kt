package com.example.mytasks.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytasks.R
import com.example.mytasks.common.constants.ScreenFilterConstants
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.listener.ValidationListener
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
    private val _validation = MutableLiveData<ValidationListener>()
    val validation: LiveData<ValidationListener> = _validation

    private val _listTasks = MutableLiveData<List<TaskModel>?>()
    val listTasks: LiveData<List<TaskModel>?> = _listTasks

    // REPOSITORY
    private val mTaskRepository: TaskRepository = TaskRepository(application)

    fun delete(task: TaskModel, screen: Int) {

        task.image.let {
            if (!it.isNullOrEmpty()) {
                deletePhoto(it)
            }
        }

        task.id?.let { deleteTask(it, screen) }
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
            ScreenFilterConstants.SCREEN.TODAY -> getListAllTasks(user.uid)
            ScreenFilterConstants.SCREEN.DONE -> getListDoneTasks(user.uid)
        }
    }

    private fun deletePhoto(imageUrl: String) {
        val listener = object : ApiCallbackListener<String> {

            override fun onSuccess(result: String?, statusCode: Int?) {
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
            }
        }

        mTaskRepository.deletePhoto(imageUrl, listener)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 0, 0, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 23, 59, 59)
        return calendar.timeInMillis
    }

    fun getListAllTasks(id: String) {

        var startDay = getStartOfDay()
        var endDay = getEndOfDay()

        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
            }
        }

        mTaskRepository.getListTask(id, startDay, endDay, listener)
    }

    fun getListDoneTasks(id: String) {

        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
            }
        }

        mTaskRepository.getListDoneTasks(id, listener)
    }

    fun getTaskById(id: String) {}

    fun onChangeCompleteTaskClick(id: String, statusTask: Boolean, screen: Int) {

        val listener = object : ApiCallbackListener<String> {
            override fun onSuccess(result: String?, statusCode: Int?) {
                getListByTypeScreen(screen)
                _validation.value =
                    ValidationListener(successMessage = context.getString(R.string.task_updated_successfully))
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
            }
        }

        mTaskRepository.onChangeCompleteTaskClick(id, statusTask, listener)
    }
}
