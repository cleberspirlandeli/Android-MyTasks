package com.example.mytasks.ui.thisWeek

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ThisWeekViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is this week Fragment"
    }
    val text: LiveData<String> = _text
}