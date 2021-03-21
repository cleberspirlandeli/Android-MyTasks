package com.example.mytasks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.*
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;



class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        auth = FirebaseAuth.getInstance()

        servicesGoogle()
        servicesFacebook()
        listeners()
    }



    private fun listeners() {
        cardview_google.setOnClickListener(this)
        cardview_facebook.setOnClickListener(this)
    }

    private fun servicesGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun servicesFacebook() {
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    createAccountFacebookInToFirebase(loginResult.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(baseContext, "ON CANCEL", Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException) {
                    val erro = error.message
                    //Util.CustomMessageError(erro, baseContext)
                    Toast.makeText(baseContext, erro, Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cardview_google -> signInGoogle()
            R.id.cardview_facebook -> signInFacebook()
        }
    }

    private fun signInGoogle() {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)

        if(googleSignInAccount == null) {
            // user not connected
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 555)
        } else {
            // user connected
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }
    }

    private fun signInFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
    }

    // Retorno da tela de login do Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 555) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, getString(R.string.txt_err_connect_a_google), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.txt_err_create_account_google), Toast.LENGTH_SHORT).show();
                }
            }
    }

    private fun createAccountFacebookInToFirebase(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(
                this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            baseContext,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }
}
