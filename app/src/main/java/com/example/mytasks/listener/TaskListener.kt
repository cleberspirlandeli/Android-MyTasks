package com.example.mytasks.listener

interface TaskListener {

    /**
     * View / Update
     * Click para Visualizaçao / Ediçao
     */
    fun onViewTaskClick(id: String)

    /**
     * Delete
     * Remoção
     */
    fun onDeleteTaskClick(id: String)

    /**
     * Done Task
     * Completa tarefa
     */
    fun onDoneTaskClick(id: String)

    /**
     * Undo Task
     * Descompleta tarefa
     */
    fun onUndoTaskClick(id: String)

}