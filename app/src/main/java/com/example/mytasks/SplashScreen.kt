package com.example.mytasks

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SplashScreen : AppCompatActivity(), Runnable {
    private var mThread: Thread? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = Firebase.auth

        mThread = Thread(this)
        mThread!!.start()


        privateKeyHash()


    }

    private fun privateKeyHash() {
        try {
            val info = packageManager.getPackageInfo("com.example.mytasks", PackageManager.GET_SIGNATURES)
            for(signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        }
        catch (e: PackageManager.NameNotFoundException) {

        }
        catch (e: NoSuchAlgorithmException) {

        }
    }

    override fun run() {
        try {
            val user = auth.currentUser

            Thread.sleep(2000)

            if (user == null || !user.isEmailVerified) {
                startActivity(Intent(baseContext, AuthenticationActivity::class.java))
            } else {
                startActivity(Intent(baseContext, MainActivity::class.java))
            }
            finish()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}