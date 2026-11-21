package com.example.finalprojectdraft

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dbHelper = DatabaseHelper(this)
        currentUserId = dbHelper.getDefaultUserId()

        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val btnClearData = findViewById<Button>(R.id.btnClearData)
        val btnBack = findViewById<Button>(R.id.btnBack)

        switchDarkMode.isChecked = isDarkMode

        btnBack.setOnClickListener {
            finish()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnClearData.setOnClickListener {
            showClearDataConfirmationDialog()
        }
    }

    private fun showClearDataConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Media Entries")
            .setMessage("Are you sure you want to delete all saved media entries? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                val success = dbHelper.clearAllEntries(currentUserId)
                if (success) {
                    Toast.makeText(this, "All entries have been cleared.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to clear entries.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
