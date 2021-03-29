package com.example.mytasks.listener

class ValidationListener(successMessage: String? = "", errorMessage: String? = "") {
    private var mStatus: Boolean = true
    private var mSuccessMessage: String = ""
    private var mErrorMessage: String = ""

    init {
        if (!errorMessage.isNullOrEmpty()) {
            mStatus = false
            mErrorMessage = errorMessage
        } else {
            mSuccessMessage = successMessage.toString()
        }
    }

    fun isSuccess() = mStatus
    fun getErrorMessage() = mErrorMessage
    fun getSuccessMessage() = mSuccessMessage
}