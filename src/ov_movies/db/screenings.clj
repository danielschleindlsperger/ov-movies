(ns ov_movies.db.screenings
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "ov_movies/db/sql/screenings.sql")