package com.example.mytasks

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ADMOB
        MobileAds.initialize(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            startActivity(Intent(this, TaskFormActivity::class.java));
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_next_week, R.id.nav_not_completed), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val navigationView : NavigationView  = findViewById(R.id.nav_view)
        val headerView : View = navigationView.getHeaderView(0)
        val navUserPhoto : ImageView = headerView.findViewById(R.id.imgUserPhoto)
        val navUsername : TextView = headerView.findViewById(R.id.txtUserName)
        val navUserEmail : TextView = headerView.findViewById(R.id.txtEmail)

        auth = Firebase.auth
        val user = auth.currentUser

        navUsername.text = user.displayName
        navUserEmail.text = user.email


        if (user.photoUrl != null) {
            Glide.with(headerView).load(user.photoUrl).into(navUserPhoto)
        }

        navView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener({ menuItem ->
            confirmLogout()
            true
        })
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_logout)
            .setMessage(R.string.message_logout)
            .setPositiveButton(R.string.yes) { dialog, which ->
                logout()
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {

        var mGoogleSignInClient: GoogleSignInClient? = null

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google
        mGoogleSignInClient.signOut()


        // Facebook
        LoginManager.getInstance().logOut()

        // Email
        // FirebaseAuth.getInstance().signOut()

        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}