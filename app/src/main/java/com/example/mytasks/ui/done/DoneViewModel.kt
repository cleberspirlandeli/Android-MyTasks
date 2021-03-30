package com.example.mytasks.ui.done

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DoneViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is done Fragment"
    }
    val text: LiveData<String> = _text
}