(ns ov-movies.movie
  (:require [ov-movies.util :refer [parse-zoned-date-time]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [honeysql.helpers]
            [honeysql-postgres.format]
            [honeysql.format :refer [format] :rename {format sql-format}]
            [clojure.data.json :as json]
            [ov-movies.database :refer [db-opts]]))

(defn rand-id [] (str/join (map (fn [_] (rand-int 10)) (range 6))))

(s/def ::id (s/with-gen (s/and string? #(= 6 (count %)))
              #(gen/fmap (fn [_] (rand-id)) (s/gen any?))))

(s/def ::title (s/and string? #(<= 3 (count %))))

(s/def ::poster (s/with-gen string?
                  #(gen/fmap (fn [uri] (str uri "/poster.jpg")) (s/gen uri?))))

(s/def ::movie (s/keys :req [::id ::title]
                       :opt [::poster]))

(def get-movies-query "
SELECT row_to_json(mov) AS movie
FROM (
  SELECT m.*,
    (SELECT json_agg(scr) FROM (SELECT * FROM screenings WHERE movie_id = m.id AND date > now()) scr) AS screenings
    FROM movies AS m) mov WHERE json_typeof(screenings) != 'null' AND blacklisted = false;")

(defn insert-movies-query [movies]
  (sql-format {:insert-into :movies
               :values      movies
               :on-conflict [:id]
               :do-nothing  []
               :returning   [:*]}))

(defn blacklist-movie-query [id]
  (sql-format {:update :movies
               :set {:blacklisted true}
               :where [:= :id id]
               :returning [:*]}))

(defn parse-dates [k v] (if (= k :date) (parse-zoned-date-time v) v))
(defn parse-pg-movie [pg-obj]
  (-> pg-obj :movie .getValue (json/read-str :key-fn keyword :value-fn parse-dates)))

(defn get-movies-with-upcoming-screenings [db]
  (map parse-pg-movie (jdbc/execute! db [get-movies-query])))

(defn insert-movies! [db movies] (jdbc/execute! db (insert-movies-query movies) db-opts))

(defn blacklist-movie! [db {id :id}] (jdbc/execute! db (blacklist-movie-query id) db-opts))