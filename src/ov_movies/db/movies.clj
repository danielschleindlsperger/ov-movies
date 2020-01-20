(ns ov_movies.db.movies
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "ov_movies/db/sql/movies.sql")
