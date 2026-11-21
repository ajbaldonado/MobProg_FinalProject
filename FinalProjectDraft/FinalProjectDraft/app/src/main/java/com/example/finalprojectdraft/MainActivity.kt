package com.example.finalprojectdraft

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val folderList = mutableListOf("Anime", "TV Series", "Manga")
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listViewFolders)
        val btnAddFolder = findViewById<Button>(R.id.btnAddFolder)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, folderList)
        listView.adapter = adapter

        btnAddFolder.setOnClickListener {
            showAddFolderDialog()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFolder = folderList[position]
            val intent = Intent(this, FolderActivity::class.java).apply {
                putExtra("FOLDER_NAME", selectedFolder)
            }
            startActivity(intent)
        }
    }

    private fun showAddFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Folder")

        val input = EditText(this)
        input.hint = "Folder Name"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                folderList.add(folderName)
                adapter.notifyDataSetChanged()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}