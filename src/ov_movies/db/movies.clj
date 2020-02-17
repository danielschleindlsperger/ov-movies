(ns ov-movies.db.movies
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "ov_movies/db/sql/movies.sql")
