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

-- A :result value of :n below will return affected rows:
-- :name get-upcoming-screenings :? :*
-- :doc Get all screenings
SELECT s.id AS id, s.date AS date, m.id AS movie_id, m.title AS movie_title, m.poster AS movie_poster
FROM screenings s
         LEFT OUTER JOIN movies m
                         ON s.movie_id = m.id
WHERE s.date > NOW() AND m.blacklisted = false
ORDER BY s.date DESC;