(ns ov-movies.database
  (:require [ov-movies.config :refer [config]]))

; In the future we can use a connection pool here.
; For now we just return the database uri that can be passed to jdbc as is.
(def db (-> config :database :connection-uri))