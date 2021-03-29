package com.example.mytasks.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytasks.R
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.listener.ValidationListener
import com.example.mytasks.service.model.TaskModel
import com.example.mytasks.service.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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

    fun delete(task: TaskModel) {

        task.image.let {
            if (!it.isNullOrEmpty()) {
                deletePhoto(it)
            }
        }

        task.id?.let { deleteTask(it) }
    }

    private fun deleteTask(id: String) {
        val listener = object : ApiCallbackListener<String> {
            override fun onSuccess(result: String?, statusCode: Int?) {
                getListTasks(user.uid)
                _validation.value =
                    ValidationListener(successMessage = context.getString(R.string.task_removed_successfully))
            }

            override fun onFailure(message: String) {
                _validation.value = ValidationListener(errorMessage = message)
            }
        }

        mTaskRepository.deleteTask(id, listener)
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

    fun getListTasks(id: String) {

        val listener = object : ApiCallbackListener<List<TaskModel>> {

            override fun onSuccess(result: List<TaskModel>?, statusCode: Int?) {
                _listTasks.value = result
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
            }
        }

        mTaskRepository.getListTask(id, listener)
    }

    fun getTaskById(id: String) {}

    fun onChangeCompleteTaskClick(id: String, statusTask: Boolean) {
        val listener = object : ApiCallbackListener<String> {
            override fun onSuccess(result: String?, statusCode: Int?) {
                getListTasks(user.uid)
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
