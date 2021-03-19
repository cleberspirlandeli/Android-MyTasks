package com.example.mytasks.ui.taskform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskFormViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Form Fragment"
    }
    val text: LiveData<String> = _text
}