(ns ov-movies.core
  (:gen-class)
  (:require [ov-movies.parse :as parse]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (pprint (parse/movies-with-original-screenings)))

(-main)