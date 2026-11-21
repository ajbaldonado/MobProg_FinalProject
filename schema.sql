CREATE TABLE if not exists USERS
(
    User_ID INT PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50) NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Date_Created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE if not exists CATEGORIES
(
    Category_ID INT PRIMARY KEY AUTO_INCREMENT,
    Category_Name VARCHAR(50) NOT NULL,
    User_ID INT NOT NULL,
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID) ON DELETE CASCADE
);

CREATE TABLE if not exists ENTRY_CATEGORIES
(
    Entry_ID INT,
    Category_ID INT,
    PRIMARY KEY (Entry_ID, Category_ID),
    FOREIGN KEY (Entry_ID) REFERENCES ENTRIES(Entry_ID) ON DELETE CASCADE,
    FOREIGN KEY (Category_ID) REFERENCES CATEGORIES(Category_ID) ON DELETE CASCADE
);

CREATE TABLE if not exists ENTRIES
(
    Entry_ID INT PRIMARY KEY AUTO_INCREMENT,
    Entry_Title VARCHAR(100) NOT NULL,
    Entry_Type VARCHAR(50) NOT NULL, -- e.g., 'TV Show', 'Movie', 'Book', etc.
    Primary_Progress INT DEFAULT 0 CHECK (Primary_Progress >= 0),
    Secondary_Progress TEXT,
    Upload_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Visited TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Linked_Entries TEXT, -- Comma-separated list of linked entry IDs
    Source TEXT,
    User_ID INT NOT NULL,
    FOREIGN KEY (User_ID) REFERENCES USERS(User_ID) ON DELETE CASCADE
);

SELECT 
    u.Username,
    e.Entry_Title,
    e.Entry_Type,
    e.Primary_Progress,
    e.Secondary_Progress,
    e.Source,
    GROUP_CONCAT(c.Category_Name, ', ') AS Assigned_Categories
FROM Entries e
JOIN Users u ON e.User_ID = u.User_ID
LEFT JOIN Entry_Categories ec ON e.Entry_ID = ec.Entry_ID
LEFT JOIN Categories c ON ec.Category_ID = c.Category_ID
GROUP BY e.Entry_ID;