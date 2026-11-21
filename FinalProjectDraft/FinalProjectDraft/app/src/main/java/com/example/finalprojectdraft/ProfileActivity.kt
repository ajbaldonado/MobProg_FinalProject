package com.example.finalprojectdraft

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
        currentUserId = dbHelper.getDefaultUserId()

        tvUsername = findViewById(R.id.tvUsername)
        tvFolderCount = findViewById(R.id.tvFolderCount)
        tvEntryCount = findViewById(R.id.tvEntryCount)

        val btnEditUsername = findViewById<Button>(R.id.btnEditUsername)
        val btnBack = findViewById<Button>(R.id.btnBack)

        loadProfileData()

        btnBack.setOnClickListener {
            finish()
        }

        btnEditUsername.setOnClickListener {
            showEditUsernameDialog()
        }
    }

    private fun loadProfileData() {
        val username = dbHelper.getUsername(currentUserId)
        tvUsername.text = username

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
}
