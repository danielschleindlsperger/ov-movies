(ns ov_movies.db.db
  (:require [ov_movies.util :refer [fetch-sm-secret]]
            [ov_movies.db.screenings :as screenings]
            [clojure.string :as str]))

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

(defn- remove-key-prefix
  "Removes all prefixes (\"prefix_key\") from the keys in the provided map.
  Not recursive."
  [m prefix]
  (into {} (map (fn [[k v]]
                  [(keyword (str/replace (name k) (str prefix "_") "")) v]) m)))

(defn normalize-relations
  "Takes a map where all properties of the related map are prefixed with the relations name,
  removes the prefix and stores the related map as a property on the initial map."
  [xs relation]
  (map (fn [s]
         (let [relation-keys (filter #(str/starts-with? (name %) (str relation "_")) (keys s))
               own-keys (filter (complement (set relation-keys)) (keys s))
               sub-map (remove-key-prefix (select-keys s relation-keys) relation)]
           (assoc (select-keys s own-keys) (keyword relation) sub-map))) xs))

(defn upcoming-screenings [] (normalize-relations (screenings/get-upcoming-screenings db-conn) "movie"))