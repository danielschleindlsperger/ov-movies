(ns ov-movies.database
  (:require [clojure.data.json :as json]
            [next.jdbc.result-set :as result-set]
            [next.jdbc.prepare :as prepare]
            [next.jdbc.date-time :as date-time]
            [migratus.core :as migratus]
            [camel-snake-kebab.core :refer
             [->kebab-case-keyword ->snake_case_string]]
            [ov-movies.config :refer [config]])
  (:import [org.postgresql.util PGobject]
           [java.sql PreparedStatement]))

(set! *warn-on-reflection* true)

(date-time/read-as-instant)

(defn as-unqualified-kebab-maps
  "Transform the keys of next.jdbc's function's result rows to kebab case.
   Usage: (next.jdbc.sql/get-by-id ds :users 1 {:builder-fn as-unqualified-kebab-maps})"
  [rs opts]
  (result-set/as-unqualified-modified-maps rs
                                           (assoc opts
                                             :qualifier-fn ->kebab-case-keyword
                                             :label-fn ->kebab-case-keyword)))

(def db-opts
  {:builder-fn as-unqualified-kebab-maps,
   :table-fn ->snake_case_string,
   :column-fn ->snake_case_string})

; In the future we can use a connection pool here.
; For now we just return the database uri that can be passed to jdbc as is.
(def db
  (-> config
      :database
      :connection-uri))
(defn- migratus-config
  []
  {:store :database,
   :migration-dir "migrations/",
   :migration-table-name "schema_migrations",
   :db db})

(defn migrate! [] (migratus/migrate (migratus-config)))

(defn rollback! [] (migratus/rollback (migratus-config)))

(defn create-migration! [name] (migratus/create (migratus-config) name))

(defn reset-migrations! [] (migratus/reset (migratus-config)))

(def ->json json/write-str)
(def <-json #(json/read-str % :key-fn ->kebab-case-keyword))

(defn ->pgobject
  "Transforms Clojure data to a PGobject that contains the data as
  JSON. PGObject type defaults to `jsonb` but can be changed via
  metadata key `:pgtype`"
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.) (.setType pgtype) (.setValue (->json x)))))

(defn <-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure
  data."
  [^PGobject v]
  (let [type (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (with-meta (<-json value) {:pgtype type})
      value)))

;; if a SQL parameter is a Clojure hash map or vector, it'll be transformed
;; to a PGobject for JSON/JSONB:
(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentMap
    (set-parameter [m ^PreparedStatement s i] (.setObject s i (->pgobject m)))
  clojure.lang.IPersistentVector
    (set-parameter [v ^PreparedStatement s i] (.setObject s i (->pgobject v))))

;; if a row contains a PGobject then we'll convert them to Clojure data
;; while reading (if column is either "json" or "jsonb" type):
(extend-protocol result-set/ReadableColumn
  PGobject
    (read-column-by-label [^PGobject v _] (<-pgobject v))
    (read-column-by-index [^PGobject v _2 _3] (<-pgobject v)))