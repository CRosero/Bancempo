package com.bancempo.activities

import android.content.ClipData
import com.bancempo.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bancempo.SignInActivity
import com.bancempo.data.User
import com.bancempo.databinding.ActivityMainBinding
import com.bancempo.models.SharedViewModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private val sharedVM: SharedViewModel by viewModels()

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val navView = findViewById<NavigationView>(R.id.nav_view)

        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        sharedVM.currentUser.observe(this){
            println("messaggio: currentUser -> ${it?.email}")
        }

        sharedVM.authUser.observe(this){
            println("messaggio: authuse -> ${it?.email}")
        }

        sharedVM.services.observe(this){
            println("messaggio: servs -> ${it.size}")
        }

        sharedVM.myAdvs.observe(this){
            println("messaggio: myadvs -> ${it.size}")
        }

        sharedVM.advs.observe(this){
            println("messaggio: advs -> ${it.size}")
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.goToTimeSlotList -> {
                    if (navController.currentDestination?.id != R.id.timeSlotListFragment) {

                        navController.navigate(R.id.timeSlotListFragment)
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.goToShowProfile -> {
                    if (navController.currentDestination?.id != R.id.showProfileFragment) {
                        navController.navigate(R.id.showProfileFragment)
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.logoutItem -> {
                    signOut()
                    //sharedVM.cleanAfterLogout()
                }
            }
            false
        }

        // Initialize Firebase Auth and check if the user is signed in
        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        sharedVM.authUser.observe(this) { firebaseUser ->
            if (firebaseUser == null) {
                Log.d("AuthListener", "----------------null user")
            } else {
                Log.d("AuthListener", "--------------------OK user")
                sharedVM.createUserIfDoesNotExists()
            }
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            sharedVM.afterLogin()
            return
        }
        else {
            findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.sign_in_button).isVisible = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //TODO FUNZIONE PER TORNARE INDIETRO DAL MENU, CAPIRE SE FUNZIONA DA UNDO OPPURE SE BISOGNA SALVARE
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.findNavController()

        return NavigationUI.navigateUp(navController, drawer)
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
    
}