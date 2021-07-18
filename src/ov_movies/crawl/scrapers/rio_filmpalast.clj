(ns ov-movies.crawl.scrapers.rio-filmpalast
  (:require [clojure.data.json :as json])
  (:import (java.time Instant)))

;; Rio Filmpalast uses kinoheld as a service provider which has a publicly accessible api
(def url "https://www.kinoheld.de/ajax/getShowsForCinemas?cinemaIds[]=748&lang=en")

(defn original?
  [screening]
  (boolean (some #(= "subtitled" (:code %)) (:flags screening))))

(defn parse-movies [json]
  (let [raw-screenings (-> json (json/read-str :key-fn keyword) :shows)]
    (->> raw-screenings
         (group-by :name)
         (map (fn [[name screenings]]
                {:title      name
                 :screenings (map (fn [scr]
                                    {:date      (-> scr :beginning :isoFull Instant/parse)
                                     :original? (original? scr)}) screenings)})))))

(defn scrape! []
  (parse-movies (slurp url)))

(comment
  (time (scrape!))
  (filter #(some :original? (:screenings %)) (scrape!)))