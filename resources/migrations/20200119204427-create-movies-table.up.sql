CREATE TABLE movies (
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    poster TEXT,
    original_lang VARCHAR(10) NOT NULL,
    cinema VARCHAR(255) NOT NULL,
    blacklisted BOOLEAN DEFAULT false
);