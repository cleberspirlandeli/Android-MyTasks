package com.example.mytasks.model

data class TaskModel(
    val userId: String? = null,
    val task: String? = null,
    val priority: Number? = null,
    val complete: Boolean? = false,
    val date: Long? = null,
    val description: String? = null,
    val image: String? = null
)
