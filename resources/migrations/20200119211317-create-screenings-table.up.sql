CREATE TABLE screenings (
    id VARCHAR(255) PRIMARY KEY NOT NULL, -- cineplex id
    movie_id varchar(255) references movies(id) ON DELETE CASCADE,
    date TIMESTAMP WITH TIME ZONE NOT NULL
);