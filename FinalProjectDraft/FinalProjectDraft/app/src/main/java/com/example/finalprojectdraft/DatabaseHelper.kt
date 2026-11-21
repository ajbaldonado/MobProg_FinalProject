package com.example.finalprojectdraft

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class CategoryItem(val id: Int, val name: String, val userId: Int)
data class MediaEntry(val id: Int, val title: String, val type: String, val userId: Int)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "media_vault.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "USERS"
        const val TABLE_CATEGORIES = "CATEGORIES"
        const val TABLE_ENTRIES = "ENTRIES"
        const val TABLE_ENTRY_CATEGORIES = "ENTRY_CATEGORIES"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                User_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Username TEXT NOT NULL,
                Password TEXT NOT NULL,
                Date_Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent()

        val createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                Category_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Category_Name TEXT NOT NULL,
                User_ID INTEGER NOT NULL,
                FOREIGN KEY (User_ID) REFERENCES $TABLE_USERS(User_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        val createEntriesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ENTRIES (
                Entry_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Entry_Title TEXT NOT NULL,
                Entry_Type TEXT NOT NULL,
                Primary_Progress INTEGER DEFAULT 0,
                Secondary_Progress TEXT,
                Upload_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Last_Visited TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Linked_Entries TEXT,
                Source TEXT,
                User_ID INTEGER NOT NULL,
                FOREIGN KEY (User_ID) REFERENCES $TABLE_USERS(User_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        val createEntryCategoriesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ENTRY_CATEGORIES (
                Entry_ID INTEGER,
                Category_ID INTEGER,
                PRIMARY KEY (Entry_ID, Category_ID),
                FOREIGN KEY (Entry_ID) REFERENCES $TABLE_ENTRIES(Entry_ID) ON DELETE CASCADE,
                FOREIGN KEY (Category_ID) REFERENCES $TABLE_CATEGORIES(Category_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createEntriesTable)
        db.execSQL(createEntryCategoriesTable)

        val userValues = ContentValues().apply {
            put("Username", "default_user")
            put("Password", "password123")
        }
        val userId = db.insert(TABLE_USERS, null, userValues)
        val validUserId = if (userId != -1L) userId.toInt() else 1

        val defaultCategories = listOf("Anime", "TV Series", "Manga", "Book", "Movie")
        for (cat in defaultCategories) {
            val catValues = ContentValues().apply {
                put("Category_Name", cat)
                put("User_ID", validUserId)
            }
            db.insert(TABLE_CATEGORIES, null, catValues)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ENTRY_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ENTRIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun getDefaultUserId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT User_ID FROM $TABLE_USERS LIMIT 1", null)
        var id = 1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("User_ID"))
        }
        cursor.close()
        return id
    }

    fun getAllCategories(userId: Int): List<CategoryItem> {
        val list = mutableListOf<CategoryItem>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT Category_ID, Category_Name, User_ID FROM $TABLE_CATEGORIES WHERE User_ID = ? ORDER BY Category_ID ASC",
            arrayOf(userId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("Category_ID"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("Category_Name"))
                val uId = cursor.getInt(cursor.getColumnIndexOrThrow("User_ID"))
                list.add(CategoryItem(id, name, uId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addCategory(categoryName: String, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("Category_Name", categoryName)
            put("User_ID", userId)
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getEntriesForCategory(categoryId: Int): List<MediaEntry> {
        val list = mutableListOf<MediaEntry>()
        val db = readableDatabase
        val query = """
            SELECT e.Entry_ID, e.Entry_Title, e.Entry_Type, e.User_ID
            FROM $TABLE_ENTRIES e
            JOIN $TABLE_ENTRY_CATEGORIES ec ON e.Entry_ID = ec.Entry_ID
            WHERE ec.Category_ID = ?
            ORDER BY e.Entry_ID DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("Entry_ID"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("Entry_Title"))
                val type = cursor.getString(cursor.getColumnIndexOrThrow("Entry_Type"))
                val uId = cursor.getInt(cursor.getColumnIndexOrThrow("User_ID"))
                list.add(MediaEntry(id, title, type, uId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addEntry(title: String, type: String, categoryId: Int, userId: Int): Long {
        val db = writableDatabase
        db.beginTransaction()
        var entryId: Long = -1
        try {
            val values = ContentValues().apply {
                put("Entry_Title", title)
                put("Entry_Type", type)
                put("User_ID", userId)
            }
            entryId = db.insert(TABLE_ENTRIES, null, values)

            if (entryId != -1L) {
                val ecValues = ContentValues().apply {
                    put("Entry_ID", entryId)
                    put("Category_ID", categoryId)
                }
                db.insert(TABLE_ENTRY_CATEGORIES, null, ecValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return entryId
    }

    fun getUsername(userId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT Username FROM $TABLE_USERS WHERE User_ID = ?", arrayOf(userId.toString()))
        var name = "User"
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("Username"))
        }
        cursor.close()
        return name
    }

    fun updateUsername(userId: Int, newName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("Username", newName)
        }
        val rows = db.update(TABLE_USERS, values, "User_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    fun getUserStats(userId: Int): Pair<Int, Int> {
        val db = readableDatabase
        var categoryCount = 0
        var entryCount = 0

        val catCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES WHERE User_ID = ?", arrayOf(userId.toString()))
        if (catCursor.moveToFirst()) {
            categoryCount = catCursor.getInt(0)
        }
        catCursor.close()

        val entryCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ENTRIES WHERE User_ID = ?", arrayOf(userId.toString()))
        if (entryCursor.moveToFirst()) {
            entryCount = entryCursor.getInt(0)
        }
        entryCursor.close()

        return Pair(categoryCount, entryCount)
    }

    fun clearAllEntries(userId: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_ENTRIES, "User_ID = ?", arrayOf(userId.toString()))
        return rows >= 0
    }
}
