package com.example.easyfood.ui.activites

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.easyfood.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgetPassword : AppCompatActivity() {
    lateinit var forgetPasswordBinding: ActivityForgetPasswordBinding

    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        forgetPasswordBinding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(forgetPasswordBinding.root)

        forgetPasswordBinding.forgetbtn.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            val email = forgetPasswordBinding.forgetpasstext.text.toString().trim()
            if (email.isEmpty()) {
                forgetPasswordBinding.forgetpasstext.error = "Email is required"
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email sent to reset password", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        forgetPasswordBinding.forgetpasstext.error = "Email is not registered"
                    } else {
                        forgetPasswordBinding.forgetpasstext.error =
                            "Failed to send reset email: ${exception?.localizedMessage}"
                    }
                }
            }
        }
    }
}