(ns dev
  (:refer-clojure :exclude [test])
  (:require [eftest.runner :as eftest]
            [next.jdbc :as jdbc]
            [org.httpkit.client :refer [request]]
            [ov-movies.database :as database]
            [ov-movies.config :refer [config]]
            [ov-movies.crawl.crawler :as crawler]
            [ov-movies.web-server :refer [restart-server]]))

(defn restart []
  (restart-server))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(defn crawl! []
  (let [movie-db-api-key (get-in config [:movie-db :api-key])]
    (crawler/crawl! database/db movie-db-api-key)))

(defn query [stmt] (jdbc/execute! database/db [stmt]))

(defn http [url] (deref (request {:url url}) 10000 "Timed out after 10000ms"))

;; run once initially when jacking-in
(restart)

(comment
  (database/migrate!)
  (crawl!))