package com.example.mytasks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity(), Runnable {
    private var mThread: Thread? = null
//    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

//        mFirebaseAuth = FirebaseAuth.getInstance()
        mThread = Thread(this)
        mThread!!.start()

    }

    override fun run() {
//        try {
//            val user: FirebaseUser = mFirebaseAuth.getCurrentUser()
//            Thread.sleep(2000)
//            if (user == null || !user.isEmailVerified()) {
//                startActivity(Intent(baseContext, MainActivity::class.java))
//            } else {
//                startActivity(Intent(baseContext, MainActivity::class.java))
//            }
//            finish()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }

        try {
            Thread.sleep(2000)
            startActivity(Intent(baseContext, MainActivity::class.java))
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}