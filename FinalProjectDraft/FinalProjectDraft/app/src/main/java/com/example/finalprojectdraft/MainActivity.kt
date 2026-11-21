package com.example.finalprojectdraft

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var gridAdapter: CategoryGridAdapter
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        currentUserId = dbHelper.getDefaultUserId()

        val gridView = findViewById<GridView>(R.id.gridViewFolders)
        val btnAddFolder = findViewById<Button>(R.id.btnAddFolder)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        gridAdapter = CategoryGridAdapter(this, categoryList)
        gridView.adapter = gridAdapter

        loadCategoriesFromDatabase()

        btnAddFolder.setOnClickListener {
            showAddFolderDialog()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categoryList[position]
            val intent = Intent(this, FolderActivity::class.java).apply {
                putExtra("CATEGORY_ID", selectedCategory.id)
                putExtra("FOLDER_NAME", selectedCategory.name)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategoriesFromDatabase()
    }

    private fun loadCategoriesFromDatabase() {
        categoryList.clear()
        val categories = dbHelper.getAllCategories(currentUserId)
        categoryList.addAll(categories)
        if (::gridAdapter.isInitialized) {
            gridAdapter.notifyDataSetChanged()
        }
    }

    private fun showAddFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Category")

        val input = EditText(this)
        input.hint = "Category Name (e.g. Movies, Anime...)"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                dbHelper.addCategory(folderName, currentUserId)
                loadCategoriesFromDatabase()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}

class CategoryGridAdapter(context: Context, private val categories: List<CategoryItem>) :
    ArrayAdapter<CategoryItem>(context, R.layout.item_category_grid, categories) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_category_grid, parent, false)
        val category = getItem(position)!!

        val tvIcon = view.findViewById<TextView>(R.id.tvCategoryIcon)
        val tvName = view.findViewById<TextView>(R.id.tvCategoryName)

        tvName.text = category.name
        tvIcon.text = getIconForCategory(category.name)

        return view
    }

    private fun getIconForCategory(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("anime") -> "⛩️"
            lower.contains("tv") || lower.contains("series") -> "📺"
            lower.contains("manga") -> "📖"
            lower.contains("book") -> "📚"
            lower.contains("movie") || lower.contains("film") -> "🎥"
            lower.contains("game") -> "🎮"
            lower.contains("music") -> "🎵"
            else -> "📁"
        }
    }
}
