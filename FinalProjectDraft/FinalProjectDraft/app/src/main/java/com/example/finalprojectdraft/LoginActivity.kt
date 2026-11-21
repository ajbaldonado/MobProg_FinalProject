package com.example.finalprojectdraft

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPreferences.getInt("logged_in_user_id", -1)
        if (loggedInUserId != -1) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        dbHelper = DatabaseHelper(this)

        val inputUsername = findViewById<EditText>(R.id.inputUsername)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGuest = findViewById<Button>(R.id.btnGuest)

        btnLogin.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = dbHelper.loginUser(username, password)
            if (userId != -1) {
                sharedPreferences.edit().putInt("logged_in_user_id", userId).apply()
                Toast.makeText(this, "Welcome back, $username!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password to register", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUserId = dbHelper.registerUser(username, password)
            if (newUserId != -1L) {
                sharedPreferences.edit().putInt("logged_in_user_id", newUserId.toInt()).apply()
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Username already exists. Choose another.", Toast.LENGTH_SHORT).show()
            }
        }

        btnGuest.setOnClickListener {
            val guestId = dbHelper.getOrCreateGuestUserId()
            sharedPreferences.edit().putInt("logged_in_user_id", guestId).apply()
            Toast.makeText(this, "Logged in as Guest", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
