(ns ov-movies.crawl.crawler
  (:require [next.jdbc :as jdbc]
            [ov-movies.movie :refer [insert-movies!]]
            [ov-movies.screening :refer [insert-screenings!]]
            [ov-movies.util :refer [parse-date]]
            [ov-movies.crawl.scrapers.cineplex-neufahrn :as cineplex-neufahrn]
            [ov-movies.crawl.scrapers.cineplex-germering :as cineplex-germering]
            [taoensso.timbre :as log]))

(defn normalize-movie [movie]
  (let [dates (:original-dates movie)]
    (map #(-> %
              (assoc :movie_id (:id movie))
              (update :date parse-date)) dates)))

(defn normalize-scraped
  "Takes a list of parsed movies and returns a map of
  :movies (with their dates parsed) and
  :screenings (with :movie_id)"
  [xs]
  {:movies (map #(dissoc % :original-dates) xs)
   :screenings (flatten (map normalize-movie xs))})

(defn- assoc-movie [movie]
  (map #(assoc % :movie-id (:id movie)) (:screenings movie)))

(defn- add-movie-metadata [movie]
  ;; TODO: fetch from https://www.themoviedb.org/documentation/api
  (assoc movie :original-lang "de"))

(def ^:private cinemas [[:cineplex-germering cineplex-germering/scrape!]
                        [:cineplex-neufahrn cineplex-neufahrn/scrape!]])

(defn crawl! [db]
  (let [movies (->> cinemas
                    (map (fn [[cinema scrape!]]
                           (map #(assoc % :cinema (name cinema)) (scrape!))))
                    flatten
                    (map add-movie-metadata))
        screenings (mapcat assoc-movie movies)]
    (jdbc/with-transaction [tx db]
      (let [new-movies (insert-movies! tx (map #(dissoc % :screenings) movies))
            new-screenings (insert-screenings! tx screenings)]
        (log/info "inserted" (count new-movies) "new movies")
        (log/info "inserted" (count new-screenings) "new screenings")))))

(comment
  (def ds (get-in (var-get (requiring-resolve 'ov-movies.config/config)) [:database :connection-uri]))
  (time (crawl! ds)))