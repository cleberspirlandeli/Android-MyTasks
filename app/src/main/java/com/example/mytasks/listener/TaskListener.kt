package com.example.mytasks.listener

import com.example.mytasks.service.model.TaskModel

interface TaskListener {

    /**
     * View / Update
     * Click para Visualizaçao / Ediçao
     */
    fun onViewTaskClick(task: TaskModel)

    /**
     * Delete
     * Remoção
     */
    fun onDeleteTaskClick(task: TaskModel)

    /**
     * Done Task
     * Completa tarefa
     */
    fun onChangeCompleteTaskClick(id: String, statusTask: Boolean)

}