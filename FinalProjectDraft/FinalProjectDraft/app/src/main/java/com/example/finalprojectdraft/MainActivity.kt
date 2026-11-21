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
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("logged_in_user_id", -1)
        if (currentUserId == -1) {
            currentUserId = dbHelper.getDefaultUserId()
        }

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

        gridView.setOnItemLongClickListener { _, _, position, _ ->
            val category = categoryList[position]
            val options = arrayOf("Rename Category", "Delete Category")
            AlertDialog.Builder(this)
                .setTitle(category.name)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showRenameCategoryDialog(category)
                        1 -> showDeleteCategoryConfirmation(category)
                    }
                }
                .show()
            true
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

    private fun showRenameCategoryDialog(category: CategoryItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Category")
        val input = EditText(this)
        input.setText(category.name)
        builder.setView(input)
        builder.setPositiveButton("Save") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                dbHelper.updateCategoryName(category.id, newName)
                loadCategoriesFromDatabase()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showDeleteCategoryConfirmation(category: CategoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}' and all its entries?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteCategory(category.id)
                loadCategoriesFromDatabase()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
