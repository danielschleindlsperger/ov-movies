(ns ov-movies.main
  (:gen-class)
  (:require [ov-movies.web-server :refer [start-server]]
            [ov-movies.database :as database]
            [ov-movies.logging :refer [set-json-logging!]]))

(set-json-logging!)

(defn -main [& _args] (database/migrate!) (start-server))
