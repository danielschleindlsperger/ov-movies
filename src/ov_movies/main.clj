(ns ov-movies.main
  (:gen-class)
  (:require [ov-movies.web-server :refer [start-server]]))

(defn -main [& _args] (start-server))
