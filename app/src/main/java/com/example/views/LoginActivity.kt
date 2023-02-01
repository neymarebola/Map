package com.example.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.kotlinmap.R
import com.example.kotlinmap.databinding.ActivityLoginBinding
import com.example.kotlinmap.databinding.ActivitySignUpBinding
import com.example.models.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        auth = FirebaseAuth.getInstance()
        var email = binding.edtEmail.text
        var password = binding.edtPassword.text

        binding.btnLogin.setOnClickListener {
            login(email.toString(), password.toString())
        }

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        goToMainActivity(user)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun goToMainActivity(user: FirebaseUser) {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        var currentUser = auth.currentUser
        if (currentUser != null) {
            // go to MainActivity
            goToMainActivity(currentUser)
        }
    }
}