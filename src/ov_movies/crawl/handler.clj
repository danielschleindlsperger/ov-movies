(ns ov-movies.crawl.handler
  (:require [compojure.core :refer [GET]]
            [integrant.core :as ig]))

(defmethod ig/init-key :ov-movies.crawl/handler [_ {db :db}]
  (GET "/crawl" [req]
    (println req)
    "hello world!"))
