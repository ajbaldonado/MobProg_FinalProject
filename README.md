# Omnitainment

**All-in-One Entertainment Backlog & Progress Tracker for Android**

Omnitainment is a native Android database application designed to unify personal entertainment tracking across multiple mediums—including **Anime, Manga, Light Novels, Movies, TV Shows, and Books**. Instead of juggling separate apps or losing track of exact episodes, chapters, or timestamps across streaming and reading platforms, users can organize, link, and monitor their entire backlog in one local database.

---

## Key Features

* **Unified Multi-Medium Catalog:** Manage entries across diverse formats (Anime, Manga, Books, Movies, TV Shows) with full Create, Read, Update, and Delete (CRUD) operations.
* **Granular Dual-Progress Tracking:**
  * **Primary Progress:** Numeric tracking for episodes watched or chapters read.
  * **Secondary Progress:** Granular text tracking for exact video timestamps (e.g., `01:23:45`), season/episode codes (`S02E05`), or page numbers.
* **Custom Categorization (Many-to-Many):** Create user-defined categories (e.g., *Currently Watching*, *Favorites*, *Plan to Read*) and assign multiple tags to any entry.
* **Cross-Media Linking:** Connect related entries across different mediums (e.g., linking a movie adaptation directly to its source novel or manga).
* **Smart Online Autocomplete:** Integrated live search suggestions powered by public REST APIs based on the selected media type:
  * **Jikan API (v4):** Anime and Manga metadata lookup.
  * **Google Books API:** Books and Light Novels lookup.
  * **iTunes Search API:** Movies and TV Shows lookup.
* **Local User Profiles & Analytics:** Multi-user support with isolated libraries, visit history timestamps, and tracker summarization metrics.

---

## Tech Stack & Architecture

* **Language:** Kotlin
* **Platform:** Android SDK (Android Studio)
* **Local Database:** SQLite (via Room Persistence Library / `SQLiteOpenHelper`)
* **Networking & JSON Parsing:** `HttpURLConnection`, `org.json`, background threading with main-thread `Looper` callbacks and request debouncing
* **External APIs:** Jikan REST API v4, Google Books Volumes API, iTunes Search API

---

## Database Schema

The local SQLite database enforces foreign key constraints (`PRAGMA foreign_keys = ON`) across four normalized tables:

| Table Name | Description | Key Columns |
| :--- | :--- | :--- |
| **`Users`** | Stores local user account credentials and creation metadata. | `User_ID` (PK), `Username`, `Password`, `Date_Created` |
| **`Categories`** | Stores user-specific custom tags and watchlist shelves. | `Category_ID` (PK), `Category_Name`, `User_ID` (FK) |
| **`Entries`** | Stores all tracked media items, progress metrics, sources, and links. | `Entry_ID` (PK), `Entry_Name`, `Entry_Type`, `Primary_Progress`, `Secondary_Progress`, `Upload_Date`, `Last_Visited`, `Linked_Entries`, `Source`, `User_ID` (FK) |
| **`Entry_Categories`** | Junction table resolving the many-to-many relationship between entries and categories. | `Entry_ID` (PK, FK), `Category_ID` (PK, FK)[cite: 1] |

---

## Project Structure

```text
app/src/main/
├── java/com/example/finalprojectdraft/
│   ├── data/
│   │   ├── entities/          # Database table models (User, Category, Entry, EntryCategoryCrossRef)
│   │   ├── dao/               # Data Access Objects & SQL queries
│   │   └── AppDatabase.kt     # SQLite / Room database singleton
│   ├── MediaApiHelper.kt      # Debounced multi-API autocomplete helper
│   └── MainActivity.kt        # Main UI and navigation controller
├── res/
│   ├── layout/                # XML UI layouts
│   └── values/                # Strings, colors, and themes
└── AndroidManifest.xml        # App configuration & INTERNET permission
```
