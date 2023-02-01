package com.example.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.fragments.FriendsFragment
import com.example.fragments.LocationFragment
import com.example.fragments.ProfileFragment
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import java.io.DataInput

class MainActivity : AppCompatActivity() {
    private var auth = Firebase.auth
    private var currentUid = ""
    private lateinit var database: FirebaseDatabase

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        currentUid = auth.currentUser?.uid ?: ""
        database = Firebase.database

        supportFragmentManager.beginTransaction().replace(R.id.frame_container, LocationFragment()).commit()
        setUpBottomNavigation()
    }

    private fun setUpBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_location -> {
                    loadFragment(LocationFragment())
                    true
                }
                R.id.item_people -> {
                    loadFragment(FriendsFragment())
                    true
                }
                R.id.item_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
            }
            true
        }
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container,fragment)
        transaction.commit()
    }
}