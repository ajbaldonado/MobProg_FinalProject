package com.example.finalprojectdraft

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var currentUserId: Int = 1

    private lateinit var tvUsername: TextView
    private lateinit var tvFolderCount: TextView
    private lateinit var tvEntryCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("logged_in_user_id", 1)
        if (currentUserId == -1) {
            currentUserId = dbHelper.getDefaultUserId()
        }

        tvUsername = findViewById(R.id.tvUsername)
        tvFolderCount = findViewById(R.id.tvFolderCount)
        tvEntryCount = findViewById(R.id.tvEntryCount)

        val btnEditUsername = findViewById<Button>(R.id.btnEditUsername)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnBack = findViewById<Button>(R.id.btnBack)

        loadProfileData()

        btnBack.setOnClickListener {
            finish()
        }

        btnEditUsername.setOnClickListener {
            showEditUsernameDialog()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadProfileData() {
        val username = dbHelper.getUsername(currentUserId)
        tvUsername.text = username

        if (username.equals("Guest", ignoreCase = true)) {
            val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
            btnChangePassword.visibility = android.view.View.GONE
        }

        val stats = dbHelper.getUserStats(currentUserId)
        tvFolderCount.text = stats.first.toString()
        tvEntryCount.text = stats.second.toString()
    }

    private fun showEditUsernameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Username")

        val input = EditText(this)
        input.setText(tvUsername.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newUsername = input.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                dbHelper.updateUsername(currentUserId, newUsername)
                tvUsername.text = newUsername
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Password")

        val input = EditText(this)
        input.hint = "New Password"
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newPassword = input.text.toString().trim()
            if (newPassword.isNotEmpty()) {
                dbHelper.updatePassword(currentUserId, newPassword)
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
