(ns ov-movies.database
  (:require [next.jdbc.result-set :as result-set]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [next.jdbc.date-time]
            [ov-movies.config :refer [config]]))

(set! *warn-on-reflection* true)

(defn as-unqualified-kebab-maps
  "Transform the keys of next.jdbc's function's result rows to kebab case.
   Usage: (next.jdbc.sql/get-by-id ds :users 1 {:builder-fn as-unqualified-kebab-maps})"
  [rs opts]
  (result-set/as-unqualified-modified-maps rs (assoc opts :qualifier-fn ->kebab-case-keyword :label-fn ->kebab-case-keyword)))

(def db-opts {:builder-fn as-unqualified-kebab-maps})

; In the future we can use a connection pool here.
; For now we just return the database uri that can be passed to jdbc as is.
(def db (-> config :database :connection-uri))