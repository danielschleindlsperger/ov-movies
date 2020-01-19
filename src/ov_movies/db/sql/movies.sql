-- src/ov_movies/db/sql/movies.sql

-- A :result value of :n below will return affected rows:
-- :name insert-movie :! :n
-- :doc Insert a single movie returning affected row count
INSERT INTO movies (id, name, poster_url)
VALUES (:id, :name, :poster-url) ON CONFLICT (id) DO NOTHING;
