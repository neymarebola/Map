package com.example.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.ActivitySignUpBinding
import com.example.models.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        auth = Firebase.auth
        var email = binding.edtEmail.text
        var password = binding.edtPassword.text
        var phoneN = binding.edtPhoneNumber.text

        binding.btnSignUp.setOnClickListener {
            if (!TextUtils.isEmpty(email.toString()) && !TextUtils.isEmpty(password.toString()) && !TextUtils.isEmpty(
                    phoneN.toString())
            ) {
                createNewAccount(email.toString().trim(),
                    password.toString().trim(),
                    phoneN.toString().trim())
            }
        }

    }

    private fun createNewAccount(email: String, password: String, phoneNumber: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "createUserWithEmail:success")
                    // neu dang ki thanh cong, chuyen thong tin account sang man hinh SetUpProfileActivity
                    Toast.makeText(this, "Dang ki thanh cong", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    var account = Account()
                    if (user != null) {
                        account.id = user.uid
                        account.email = email
                        account.password = password
                        account.phoneNumber = phoneNumber
                        goToSetUpProfileActivity(account)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(baseContext, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                    Toast.makeText(this, "dang ki that bai", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener(this) { it ->
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToSetUpProfileActivity(account: Account) {
        var intent = Intent(this, SetUpProfileActivity::class.java)
        intent.putExtra("account", account)
        startActivity(intent)
        finish()
    }

}