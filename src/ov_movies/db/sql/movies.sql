-- src/ov_movies/db/sql/movies.sql

-- :name insert-movie :returning-execute
-- :doc Insert a single movie returning affected row's id
INSERT INTO movies (id, name, poster_url)
VALUES (:id, :name, :poster-url)
ON CONFLICT (id) DO NOTHING
RETURNING *;

-- A :result value of :n below will return affected rows:
-- :name insert-movies :returning-execute
-- :doc Insert movies returning affected rows' id
INSERT INTO movies (id, name, poster_url)
VALUES :t*:movies
ON CONFLICT (id) DO NOTHING
RETURNING *;