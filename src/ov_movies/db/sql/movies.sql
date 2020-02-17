-- src/ov-movies/db/sql/movies.sql

-- :name insert-movie :returning-execute
-- :doc Insert a single movie returning affected row
INSERT INTO movies (id, title, poster)
VALUES (:id, :title, :poster)
ON CONFLICT (id) DO NOTHING
RETURNING *;

-- :name insert-movies :returning-execute
-- :doc Insert movies returning affected rows
INSERT INTO movies (id, title, poster)
VALUES :t*:movies
ON CONFLICT (id) DO NOTHING
RETURNING *;

-- :name get-movie :? :1
-- :doc Get a movie by id
SELECT *
FROM movies
WHERE id = :id;

-- :name blacklist-movie :! :1
UPDATE movies
SET blacklisted = true
WHERE id = :id;