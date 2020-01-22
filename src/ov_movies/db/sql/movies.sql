-- src/ov_movies/db/sql/movies.sql

-- :name insert-movie :returning-execute
-- :doc Insert a single movie returning affected row
INSERT INTO movies (id, title, poster_url)
VALUES (:id, :title, :poster-url)
ON CONFLICT (id) DO NOTHING
RETURNING *;

-- A :result value of :n below will return affected rows:
-- :name insert-movies :returning-execute
-- :doc Insert movies returning affected rows
INSERT INTO movies (id, title, poster_url)
VALUES :t*:movies
ON CONFLICT (id) DO NOTHING
RETURNING *;