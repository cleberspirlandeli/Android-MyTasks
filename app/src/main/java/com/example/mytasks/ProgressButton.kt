package com.example.mytasks

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class ProgressButton(context: Context, view: View) {

    var cardView = view.findViewById<View>(R.id.cardview)
    var layout = view.findViewById<ConstraintLayout>(R.id.constraint_layout)
    var progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_btn)
    var textView = view.findViewById<TextView>(R.id.txt_progress_bar_btn)

    var fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in)


    fun buttonActivated(): Unit {
        progressBar.animation = fade_in
        progressBar.visibility = View.VISIBLE
        textView.animation = fade_in
        textView.setText(R.string.await_loading)
        textView.alpha = 0.8F
    }

    fun buttonFinish(): Unit {
        progressBar.visibility = View.GONE
        textView.setText(R.string.save)
        textView.alpha = 1.0F
    }
}