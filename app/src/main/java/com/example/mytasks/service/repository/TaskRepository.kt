package com.example.mytasks.service.repositorygetStartAndEndDayOfNextWeek

import android.content.Context
import android.util.Log
import com.example.mytasks.listener.ApiCallbackListener
import com.example.mytasks.service.model.GenericModel
import com.example.mytasks.service.model.TaskModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class TaskRepository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val tasksRef = database.collection("tasks")
    private val storage = FirebaseStorage.getInstance()

    fun getListTask(
        uid: String,
        startDay: Long,
        endDay: Long,
        cb: ApiCallbackListener<List<TaskModel>>
    ) {
        var taskList: MutableList<TaskModel> = ArrayList()

        tasksRef
            .whereEqualTo("userId", uid)
            .whereGreaterThanOrEqualTo("date", startDay)
            .whereLessThanOrEqualTo("date", endDay)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Firestore Success", "${document.id} => ${document.data}")
                    val task: TaskModel =
                        document.toObject(TaskModel::class.java).withId(document.id)
                    taskList.add(task)
                }
                cb.onSuccess(taskList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Failure", "Error getting documents: ", exception)
                cb.onFailure("Error getting documents: ${exception.toString()}")
            }
    }

    fun deleteTask(id: String, cb: ApiCallbackListener<String>) {
        tasksRef
            .document(id)
            .delete()
            .addOnSuccessListener {
                cb.onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("deleteTask", "Error deleting document", e)
                cb.onFailure("Error deleting document: ${e.toString()}")
            }
    }

    fun deletePhoto(imageUrl: String, cb: ApiCallbackListener<String>) {
        val imagesRef = storage.getReferenceFromUrl(imageUrl)

        imagesRef
            .delete()
            .addOnSuccessListener {
                // File deleted successfully
            }
            .addOnFailureListener {
                cb.onFailure("Erro ao deletar imagem: ${it.message.toString()}")
            }
    }

    fun onChangeCompleteTaskClick(
        id: String,
        statusTask: Boolean,
        cb: ApiCallbackListener<String>
    ) {
        tasksRef
            .document(id)
            .update("complete", statusTask)
            .addOnSuccessListener {
                cb.onSuccess()
            }
            .addOnFailureListener {
                cb.onFailure(it.message.toString())
            }
    }

    fun getListDoneTasks(id: String, cb: ApiCallbackListener<List<TaskModel>>) {
        var taskList: MutableList<TaskModel> = ArrayList()

        tasksRef
            .whereEqualTo("userId", id)
            .whereEqualTo("complete", true)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Firestore Success", "${document.id} => ${document.data}")
                    val task: TaskModel =
                        document.toObject(TaskModel::class.java).withId(document.id)
                    taskList.add(task)
                }
                cb.onSuccess(taskList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Failure", "Error getting documents: ", exception)
                cb.onFailure("Error getting documents: ${exception.toString()}")
            }
    }

    fun getListWeekByFilterTasks(uid: String, filters: GenericModel, cb: ApiCallbackListener<List<TaskModel>>) {
        var taskList: MutableList<TaskModel> = ArrayList()

        tasksRef
            .whereEqualTo("userId", uid)
            .whereGreaterThanOrEqualTo("date", filters.startDate!!)
            .whereLessThanOrEqualTo("date", filters.endDate!!)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Firestore Success", "${document.id} => ${document.data}")
                    val task: TaskModel =
                        document.toObject(TaskModel::class.java).withId(document.id)
                    taskList.add(task)
                }
                cb.onSuccess(taskList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Failure", "Error getting documents: ", exception)
                cb.onFailure("Error getting documents: ${exception.toString()}")
            }
    }
}