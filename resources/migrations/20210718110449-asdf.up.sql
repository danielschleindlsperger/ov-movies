ALTER TABLE screenings
    DROP CONSTRAINT screenings_pkey,
    DROP COLUMN id,
    ADD PRIMARY KEY (movie_id, cinema, date);
