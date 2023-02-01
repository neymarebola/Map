package com.example.views

import android.accounts.Account
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.ActivitySearchBinding
import com.example.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var auth: FirebaseAuth = Firebase.auth
    private var database: FirebaseDatabase = Firebase.database
    private var storage: FirebaseStorage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.icSearch.setOnClickListener {
            var phoneNumber = binding.edtKey.text.toString()
            getAccountByPhoneNumber(phoneNumber)
        }


    }

    private fun getAccountByPhoneNumber(phoneNumber: String) {
        val ref = database.getReference().child("Accounts")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                for (snapshot in dataSnapshot.children) {
                    var account = snapshot.getValue(com.example.models.Account::class.java)
                    if (account?.phoneNumber == (phoneNumber.trim())) {
                        //update UI
                        binding.name.visibility = View.VISIBLE
                        binding.name.text = account?.displayName ?: ""
                        binding.txtSodt.text = "Số điện thoại: ${phoneNumber}"
                        binding.btnKb.visibility = View.VISIBLE
                        account?.let { getAvatarByUid(it.id) }

                        binding.btnKb.setOnClickListener {
                            sendMakeFriendRequest(FriendRequest(auth.currentUser?.uid ?: "", account.id))
                        }
                        break
                    }
                }
                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        })
    }

    private fun getAvatarByUid(uid: String) {
        val imageRef = storage.reference.child("avatar/${uid}")
        imageRef.downloadUrl.addOnSuccessListener {
            binding.profileImage.visibility = View.VISIBLE
            Glide.with(this).load(it).into(binding.profileImage)
        }
    }

    private fun sendMakeFriendRequest(request: FriendRequest) {
        var ref = database.getReference("FriendRequests").child(request.toId)
        ref.child(request.fromId).setValue(request).addOnSuccessListener {
            Toast.makeText(this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Đã xay ra loi", Toast.LENGTH_SHORT).show()
        }
    }
}