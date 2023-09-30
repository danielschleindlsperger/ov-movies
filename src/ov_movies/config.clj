(ns ov-movies.config
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io])
  (:import [java.time ZoneId]))

(def config
  (merge {:timezone (ZoneId/of "Europe/Paris")}
         (read-config (io/resource "ov_movies/config.edn"))))