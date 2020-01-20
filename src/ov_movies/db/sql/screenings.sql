-- src/ov_movies/db/sql/screenings.sql

-- :name insert-screening :returning-execute
-- :doc Insert a single screening returning affected row
INSERT INTO screenings (id, movie_id, date)
VALUES (:id, :movie_id, :date)
ON CONFLICT (id) DO NOTHING
RETURNING *;

-- A :result value of :n below will return affected rows:
-- :name insert-screenings :returning-execute
-- :doc Insert multiple screenings returning affected rows
INSERT INTO screenings (id, movie_id, date)
VALUES :t*:screenings
ON CONFLICT (id) DO NOTHING
RETURNING *;