package com.example.mytasks.service.repository

import android.content.Context
import android.util.Log
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.service.model.TaskModel
import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val tasksRef = database.collection("tasks")

    private var taskList: MutableList<TaskModel> = ArrayList()

    fun getListTask(cb: ApiCallbackListener<List<TaskModel>>) {
        tasksRef
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Firestore Success", "${document.id} => ${document.data}")
                    val task: TaskModel =
                        document.toObject(TaskModel::class.java).withId(document.id)
                    taskList.add(task)
                }
                cb.onSuccess(taskList, 200)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Failure", "Error getting documents: ", exception)
            }
    }
}