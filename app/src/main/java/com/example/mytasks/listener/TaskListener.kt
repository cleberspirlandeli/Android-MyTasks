package com.example.mytasks.listener

interface TaskListener {

    /**
     * Update
     * Click para edição
     */
    fun onUpdateTaskClick(id: Int)

    /**
     * Delete
     * Remoção
     */
    fun onDeleteTaskClick(id: Int)

    /**
     * Done Task
     * Completa tarefa
     */
    fun onCompleteTaskClick(id: Int)

    /**
     * Undo Task
     * Descompleta tarefa
     */
    fun onUndoTaskClick(id: Int)

}