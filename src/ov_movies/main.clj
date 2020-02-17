(ns ov-movies.main
  (:gen-class)
  (:require [duct.core :as duct]
            ov-movies.crawl.handler
    #_hello-duct.boundary.movie
    #_hello-duct.handler.example))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])
        profiles [:duct.profile/prod]]
    (-> (duct/resource "ov_movies/config.edn")
        (duct/read-config)
        (duct/exec-config profiles keys))))
