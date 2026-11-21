package com.example.finalprojectdraft

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FolderActivity : AppCompatActivity() {

    private val entryList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        val folderTitle = findViewById<TextView>(R.id.folderTitle)
        val inputEntryName = findViewById<EditText>(R.id.inputEntryName)
        val btnAddEntry = findViewById<Button>(R.id.btnAddEntry)
        val listViewEntries = findViewById<ListView>(R.id.listViewEntries)

        val folderName = intent.getStringExtra("FOLDER_NAME") ?: "Folder"
        folderTitle.text = folderName

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entryList)
        listViewEntries.adapter = adapter

        btnAddEntry.setOnClickListener {
            val entryName = inputEntryName.text.toString().trim()
            if (entryName.isNotEmpty()) {
                entryList.add(entryName)
                adapter.notifyDataSetChanged()
                inputEntryName.text.clear()
            }
        }
    }
}