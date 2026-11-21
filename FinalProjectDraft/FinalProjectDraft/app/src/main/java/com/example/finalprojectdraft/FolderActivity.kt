package com.example.finalprojectdraft

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FolderActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var categoryId: Int = -1
    private var currentUserId: Int = 1
    private val entryDisplayList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val suggestionList = mutableListOf<String>()
    private lateinit var suggestionAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        dbHelper = DatabaseHelper(this)
        currentUserId = dbHelper.getDefaultUserId()

        val folderTitle = findViewById<TextView>(R.id.folderTitle)
        val inputEntryName = findViewById<AutoCompleteTextView>(R.id.inputEntryName)
        val btnAddEntry = findViewById<Button>(R.id.btnAddEntry)
        val listViewEntries = findViewById<ListView>(R.id.listViewEntries)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val folderName = intent.getStringExtra("FOLDER_NAME") ?: "Folder"
        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        folderTitle.text = folderName

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entryDisplayList)
        listViewEntries.adapter = adapter

        suggestionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionList)
        inputEntryName.setAdapter(suggestionAdapter)

        loadEntriesFromDatabase()

        btnBack.setOnClickListener {
            finish()
        }

        inputEntryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.length >= 2) {
                    MediaApiHelper.searchSuggestions(folderName, query) { suggestions ->
                        suggestionList.clear()
                        suggestionList.addAll(suggestions)
                        suggestionAdapter.notifyDataSetChanged()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddEntry.setOnClickListener {
            val entryName = inputEntryName.text.toString().trim()
            if (entryName.isNotEmpty() && categoryId != -1) {
                dbHelper.addEntry(entryName, folderName, categoryId, currentUserId)
                loadEntriesFromDatabase()
                inputEntryName.text.clear()
            }
        }
    }

    private fun loadEntriesFromDatabase() {
        entryDisplayList.clear()
        if (categoryId != -1) {
            val entries = dbHelper.getEntriesForCategory(categoryId)
            entryDisplayList.addAll(entries.map { it.title })
        }
        adapter.notifyDataSetChanged()
    }
}