package com.example.mytasks.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.mytasks.R

class ProgressBarLoading(private val activity: Fragment) {


    private lateinit var isDialog: AlertDialog

    init {
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.progress_bar_loading, null)

        val builder = AlertDialog.Builder(activity.activity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
    }

    fun startLoading() {
        isDialog.show()
    }

    fun endLoading() {
        if (isDialog.isShowing) {
            isDialog.dismiss()
        }
    }
}