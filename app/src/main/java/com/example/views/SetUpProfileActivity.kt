package com.example.views

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.ActivitySetUpProfileBinding
import com.example.models.Account
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class SetUpProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetUpProfileBinding
    private lateinit var currentAccount: com.example.models.Account
    private val REQUEST_PICK_IMG_CODE = 1

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var database: FirebaseDatabase
    private var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_up_profile)
        storage = Firebase.storage
        storageRef = storage.reference
        database = Firebase.database

        // nhan duoc account tu ben signUpActivity
        currentAccount = intent.getSerializableExtra("account") as com.example.models.Account

        // them thong tin account -> save len firebase
        // them avatar va ten hien thi
        binding.selectImgAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_PICK_IMG_CODE)
        }

        binding.btnUpdate.setOnClickListener {
            var displayName = binding.edtDisplayName.text.toString()
            if (!TextUtils.isEmpty(displayName)) {
                currentAccount.displayName = displayName
                saveAccountToDatabase(currentAccount)
            }
        }

    }

    // luu 1 account len database
    private fun saveAccountToDatabase(account: Account) {
        val ref = database.reference
        ref.child("Accounts").child(account.id).setValue(account).addOnSuccessListener {
            setUpEmptyDatabaseReference()
            Toast.makeText(this, "Ho so duoc cap nhat", Toast.LENGTH_SHORT).show()
            goToMainActivity()
        }.addOnFailureListener {
            Log.d("haha", it.message.toString())
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setUpEmptyDatabaseReference() {
        // ref de luu danh sach loi moi ket ban+
        database.getReference("FriendRequests").child(auth.currentUser?.uid ?: "")
            .setValue("")

        // ref de luu danh sach ban be
        database.getReference("Friends").child(auth.currentUser?.uid ?: "")
            .setValue("")

        // ref de luu vi tri hien tai cua moi nguoi
        database.getReference("Locations").child(auth.currentUser?.uid ?: "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMG_CODE && resultCode == RESULT_OK) {
            // chon duoc avatar
            val imageRef = storageRef.child("avatar/${currentAccount.id}")
            var file = data?.data
            // tai avatar len storage
            if (file != null) {
                imageRef.putFile(file).addOnSuccessListener { it ->
                    imageRef.downloadUrl.addOnSuccessListener {
                        Glide.with(this).load(it).into(binding.selectImgAvatar)
                    }
                    Toast.makeText(this, "Tai anh thanh cong", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(this, "Tai anh failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





}