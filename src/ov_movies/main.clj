(ns ov-movies.main
  (:gen-class)
  (:require [ov-movies.web-server :refer [start-server]]))

(defn -main [& args] (start-server))
