(ns dev
  (:refer-clojure :exclude [test])
  (:require [eftest.runner :as eftest]
            [next.jdbc :as jdbc]
            [ov-movies.database :refer [db]]
            [org.httpkit.client :refer [request]]
            [ov-movies.web-server :refer [restart-server]]))

(defn restart []
  (restart-server))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(defn query [stmt] (jdbc/execute! db [stmt]))

(defn http [url] (deref (request {:url url}) 10000 "Timed out after 10000ms"))

(restart)