package com.example.mytasks.model

import java.util.*

class TaskModel {
    lateinit var Task: String
    lateinit var Priority: String
    lateinit var Date: Date
    var Complete: Boolean = false
    lateinit var Description: String
}