(ns ov-movies.screening
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [next.jdbc :as jdbc]
            [honeysql.helpers]
            [honeysql-postgres.format]
            [honeysql.format :as sql]
            [ov-movies.database :refer [db-opts]])
  (:import [java.time OffsetDateTime ZonedDateTime]))


(s/def ::id (s/with-gen (s/and string? #(-> % count (> 2)))
              #(gen/string-alphanumeric)))

(s/def ::date (s/with-gen #(= (type %) OffsetDateTime)
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
  (jdbc/execute! db (insert-screenings-query screenings) db-opts))
