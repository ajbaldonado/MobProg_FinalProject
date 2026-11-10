PRAGMA foreign_keys = ON;

-- Insert default media types
INSERT INTO media_types (type_name) VALUES 
    ('TV Show'),
    ('Movie'),
    ('Book'),
    ('Anime'),
    ('Manga');

-- Insert sample genres
INSERT INTO genres (genre_name) VALUES 
    ('Sci-Fi'),
    ('Fantasy'),
    ('Action'),
    ('Drama');

-- Insert sample media items
INSERT INTO media_items (title, type_id, platform, progress_unit, current_progress, total_units, last_timestamp, status, rating, is_favorite, notes) VALUES 
    ('Breaking Bad', 1, 'Netflix', 'Episodes', 62, 62, NULL, 'Completed', 10, 1, 'Masterpiece pacing.'),
    ('Dune: Part Two', 2, 'Cinema', 'Minutes', 166, 166, '02:46:00', 'Completed', 9, 1, 'Incredible visuals.'),
    ('The Way of Kings', 3, 'Physical', 'Pages', 450, 1007, NULL, 'In Progress', 9, 0, 'Currently at Chapter 32.'),
    ('Chainsaw Man', 5, 'Manga Plus', 'Chapters', 120, 0, NULL, 'In Progress', 8, 0, 'Ongoing series.');

-- Link genres to media items
INSERT INTO item_genres (item_id, genre_id) VALUES 
    (1, 4), -- Breaking Bad -> Drama
    (2, 1), -- Dune -> Sci-Fi
    (3, 2), -- The Way of Kings -> Fantasy
    (4, 3); -- Chainsaw Man -> Action