(ns ov-movies.main
  (:gen-class)
  (:require [ov-movies.web-server :refer [start-server]]
            [ov-movies.database :as database]))

(defn -main [& _args]
  (database/migrate!)
  (start-server))
