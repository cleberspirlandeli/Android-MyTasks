package com.example.mytasks.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.service.model.TaskModel
import com.example.mytasks.service.repository.TaskRepository

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    // OBSERVERS
    private val _listTasks = MutableLiveData<List<TaskModel>?>()
    val listTasks: MutableLiveData<List<TaskModel>?> = _listTasks

    // REPOSITORY
    private val mTaskRepository: TaskRepository = TaskRepository(application)

    fun deleteTask(id: String) {
    }

    fun completeTask(id: String) {

    }

    fun undoTask(id: String) {

    }

    fun getListTasks() {

        val listener = object : ApiCallbackListener<List<TaskModel>> {
            override fun onSuccess(result: List<TaskModel>, statusCode: Int) {
                _listTasks.value = result
            }

            override fun onFailure(message: String) {
                _listTasks.value = null
            }
        }

        val tasksList = mTaskRepository.getListTask(listener)
    }

    fun getTaskById(id: String) {

    }
}