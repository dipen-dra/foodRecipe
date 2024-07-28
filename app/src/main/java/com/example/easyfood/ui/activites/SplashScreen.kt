package com.example.easyfood.ui.activites

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.easyfood.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    lateinit var splashScreenBinding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreenBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(splashScreenBinding.root)

        // Set OnClickListener for the Get Started button
        splashScreenBinding.getStartedButton.setOnClickListener {
            // Start the main activity
            startActivity(Intent(this, SignUpActivity::class.java))
            // Finish the splash screen activity so it's removed from the back stack
            finish()
        }

        // Optional: If you still want to have the delay before auto-navigating to MainActivity
        /* Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity
            startActivity(Intent(this, MainActivity::class.java))
            // Finish the splash screen activity so it's removed from the back stack
            finish()
        }, 3000) // 3000 milliseconds = 3 seconds */
    }
}
