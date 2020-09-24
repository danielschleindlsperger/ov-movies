(ns ov-movies.assets
  (:require
   [clojure.java.io :as io]))

(def assets {:js (slurp (io/resource "js/app.js"))})
