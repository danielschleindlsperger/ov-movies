ALTER TABLE movies
    ADD COLUMN blacklisted BOOLEAN
        DEFAULT false;
