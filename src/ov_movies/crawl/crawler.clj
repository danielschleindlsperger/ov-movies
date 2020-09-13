(ns ov-movies.crawl.crawler
  (:require [next.jdbc :as jdbc]
            [ov-movies.movie :refer [insert-movies!]]
            [ov-movies.screening :refer [insert-screenings!]]
            [ov-movies.crawl.scrapers.cineplex-neufahrn :as cineplex-neufahrn]
            [ov-movies.crawl.scrapers.cineplex-germering :as cineplex-germering]
            [ov-movies.movie-api :as movie-api]
            [taoensso.timbre :as log]))

(defn- assoc-movie [movie]
  (map #(assoc % :movie-id (:id movie)) (:screenings movie)))

(defn- add-movie-metadata [api-key movie]
  (let [meta-data (movie-api/search-movie api-key (:title movie))
        original-lang (get meta-data :original-language "N/A")]
    (assoc movie :original-lang original-lang)))

(def ^:private cinemas [[:cineplex-germering cineplex-germering/scrape!]
                        [:cineplex-neufahrn cineplex-neufahrn/scrape!]])

(defn crawl! [db movie-db-api-key]
  (let [movies (->> cinemas
                    (map (fn [[cinema scrape!]]
                           (map #(assoc % :cinema (name cinema)) (scrape!))))
                    flatten
                    (map #(add-movie-metadata movie-db-api-key %)))
        screenings (mapcat assoc-movie movies)]
    (jdbc/with-transaction [tx db]
      (let [new-movies (insert-movies! tx (map #(dissoc % :screenings) movies))
            new-screenings (insert-screenings! tx screenings)]
        (log/info "inserted" (count new-movies) "new movies")
        (log/info "inserted" (count new-screenings) "new screenings")))))

(comment
  (def config (var-get (requiring-resolve 'ov-movies.config/config)))
  (def ds (get-in config [:database :connection-uri]))
  (def movie-db-api-key (get-in config [:movie-db :api-key]))
  (time (crawl! ds movie-db-api-key)))