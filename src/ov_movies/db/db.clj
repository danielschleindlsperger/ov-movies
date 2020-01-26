(ns ov_movies.db.db
  (:require [ov_movies.util :refer [fetch-sm-secret]]))

(def local-connection {:subprotocol "postgres"
                       :subname     "//localhost/ov_movies"
                       :user        "root"
                       :password    "root"})

(def db-conn
  "The database connection. Either a connection string or a connection map."
  (let [secret-id (System/getenv "DATABASE_URL_SECRET_ID")]
    (if (some? secret-id)
      (fetch-sm-secret secret-id)
      local-connection)))
