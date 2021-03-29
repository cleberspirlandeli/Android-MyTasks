package com.example.mytasks.listener

interface ApiCallbackListener<T> {
    fun onSuccess(result: T? = null, statusCode: Int? = null)
//    fun onSuccess(result: T, statusCode: Int)
    fun onFailure(message: String)
}