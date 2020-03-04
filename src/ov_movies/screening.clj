(ns ov-movies.screening
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers]
            [honeysql-postgres.format]
            [honeysql.format :as sql])
  (:import [java.time OffsetDateTime ZonedDateTime]))


(s/def ::id (s/with-gen (s/and string? #(-> % count (> 2)))
                        #(gen/string-alphanumeric)))

(s/def ::date (s/with-gen (partial type OffsetDateTime)
                          ;; TODO: generate, don't use "now()"
                          #(gen/fmap (fn [_] (.toOffsetDateTime (ZonedDateTime/now))) (s/gen any?))))

(s/def ::movie_id (s/and string? #(-> % count (> 2))))

(s/def ::screening (s/keys :req [::id ::date]
                           :opt [::movie_id]))

(defn insert-screenings-query [screenings]
  (sql/format {:insert-into :screenings
               :values      screenings
               :on-conflict [:id]
               :do-nothing  []
               :returning   [:*]}))

(defn insert-screenings! [db screenings]
  (jdbc/query db (insert-screenings-query screenings)))