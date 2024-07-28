package com.example.easyfood.ui.activites

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easyfood.databinding.ActivitySignUpBinding
import com.example.easyfood.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignUpBinding
    private val auth = FirebaseAuth.getInstance()
    private val RC_SIGN_IN = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)

        signUpBinding.buttonSignup.setOnClickListener {
            if (!signUpBinding.termsCheckBox.isChecked) {
                Toast.makeText(
                    this,
                    "You must agree to the terms and conditions",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val email = signUpBinding.emailSignupText.text.toString().trim()
            val password = signUpBinding.passwordSignupText.text.toString().trim()
            val confirmPassword = signUpBinding.confirmPasswordSignupText.text.toString().trim()
            val phoneNum = signUpBinding.phoneSignupText.text.toString().trim()

            if (validateInput(email, password, confirmPassword, phoneNum)) {
                createUserWithEmailAndPassword(email, password, phoneNum)
            }
        }

        signUpBinding.googleSignupImage.setOnClickListener {
            signInWithGoogle()
        }

        signUpBinding.textViewLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String,
        phoneNum: String
    ): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phoneNum.isEmpty() || phoneNum.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        phoneNum: String
    ) {
        signUpBinding.buttonSignup.isEnabled = false
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            signUpBinding.buttonSignup.isEnabled = true
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val user = User(email, phoneNum, password)
                val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)
                userRef.setValue(user).addOnCompleteListener { databaseTask ->
                    if (databaseTask.isSuccessful) {
                        Toast.makeText(this@SignUpActivity, "Signed up successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, "Database error: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@SignUpActivity,
                    task.exception?.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun signInWithGoogle() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(("YOUR_WEB_CLIENT_ID"))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

//data class User(
//    val email: String,
//    val phone: String,
//    val password: String
//)
