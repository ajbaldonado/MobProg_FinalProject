package com.example.finalprojectdraft

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FolderActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var categoryId: Int = -1
    private var currentUserId: Int = 1
    private val entryList = mutableListOf<MediaEntry>()
    private val entryDisplayList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val suggestionList = mutableListOf<String>()
    private lateinit var suggestionAdapter: AutoCompleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        dbHelper = DatabaseHelper(this)
        val sessionPrefs = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("logged_in_user_id", 1)
        if (currentUserId == -1) {
            currentUserId = dbHelper.getDefaultUserId()
        }

        val folderTitle = findViewById<TextView>(R.id.folderTitle)
        val inputEntryName = findViewById<AutoCompleteTextView>(R.id.inputEntryName)
        val inputSearchEntries = findViewById<EditText>(R.id.inputSearchEntries)
        val btnAddEntry = findViewById<Button>(R.id.btnAddEntry)
        val listViewEntries = findViewById<ListView>(R.id.listViewEntries)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val folderName = intent.getStringExtra("FOLDER_NAME") ?: "Folder"
        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        folderTitle.text = folderName

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entryDisplayList)
        listViewEntries.adapter = adapter

        suggestionAdapter = AutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionList)
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
                    val suggestions = dbHelper.searchCatalog(folderName, query)
                    suggestionList.clear()
                    suggestionList.addAll(suggestions)
                    suggestionAdapter.notifyDataSetChanged()
                    if (suggestions.isNotEmpty() && inputEntryName.hasFocus()) {
                        inputEntryName.showDropDown()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputSearchEntries.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filter = s?.toString()?.lowercase() ?: ""
                entryDisplayList.clear()
                if (filter.isEmpty()) {
                    entryDisplayList.addAll(entryList.map { it.title })
                } else {
                    entryDisplayList.addAll(entryList.filter { it.title.lowercase().contains(filter) }.map { it.title })
                }
                adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddEntry.setOnClickListener {
            val entryName = inputEntryName.text.toString().trim()
            if (entryName.isNotEmpty() && categoryId != -1) {
                dbHelper.addEntry(entryName, folderName, categoryId, currentUserId)
                loadEntriesFromDatabase()
                inputEntryName.text.clear()
                inputSearchEntries.text.clear()
            }
        }

        listViewEntries.setOnItemClickListener { _, _, position, _ ->
            val entry = entryList[position]
            showProgressDialog(entry)
        }

        listViewEntries.setOnItemLongClickListener { _, _, position, _ ->
            val entry = entryList[position]
            val options = arrayOf("Edit Entry Title", "Delete Entry")
            AlertDialog.Builder(this)
                .setTitle(entry.title)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showEditEntryDialog(entry)
                        1 -> showDeleteEntryConfirmation(entry)
                    }
                }
                .show()
            true
        }
    }

    private fun loadEntriesFromDatabase() {
        entryList.clear()
        entryDisplayList.clear()
        if (categoryId != -1) {
            val entries = dbHelper.getEntriesForCategory(categoryId)
            entryList.addAll(entries)
            entryDisplayList.addAll(entries.map { it.title })
        }
        adapter.notifyDataSetChanged()
    }

    private fun showProgressDialog(entry: MediaEntry) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Progress: ${entry.title}")

        val input = EditText(this)
        input.hint = "Enter progress (e.g. Episode 12, Chapter 45)"
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val progressText = input.text.toString().trim()
            if (progressText.isNotEmpty()) {
                Toast.makeText(this, "Progress updated for ${entry.title}", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showEditEntryDialog(entry: MediaEntry) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Entry Title")

        val input = EditText(this)
        input.setText(entry.title)
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newTitle = input.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                dbHelper.updateEntry(entry.id, newTitle)
                loadEntriesFromDatabase()
                Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showDeleteEntryConfirmation(entry: MediaEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete '${entry.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteEntry(entry.id)
                loadEntriesFromDatabase()
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class AutoCompleteAdapter(context: Context, resource: Int, private val objects: MutableList<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = objects
                results.count = objects.size
                return results
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
