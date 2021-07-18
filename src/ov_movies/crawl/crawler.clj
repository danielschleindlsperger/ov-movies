(ns ov-movies.crawl.crawler
  (:require [next.jdbc :as jdbc]
            [ov-movies.movie :refer [insert-movies!]]
            [ov-movies.screening :refer [insert-screenings! remove-screenings-for-movies!]]
            [ov-movies.crawl.scrapers.cineplex-neufahrn :as cineplex-neufahrn]
            [ov-movies.crawl.scrapers.cineplex-germering :as cineplex-germering]
            [ov-movies.crawl.scrapers.cadillac-veranda :as cadillac-veranda]
            [ov-movies.movie-api :as movie-api]
            [taoensso.timbre :as log]))

(defn- add-movie-metadata [api-key movie]
  (let [meta-data (movie-api/search-movie api-key (:title movie))
        original-lang (:original-language meta-data)
        id (str (:id meta-data))
        screenings (map #(assoc % :movie-id id) (:screenings movie))]
    (assoc movie :original-lang original-lang :id id :screenings screenings)))

(def cinemas [[:cineplex-germering cineplex-germering/scrape!]
              [:cineplex-neufahrn cineplex-neufahrn/scrape!]
              [:cadillac-veranda cadillac-veranda/scrape!]])

(defn crawl! [db movie-db-api-key]
  (let [movies (->> cinemas
                    (map (fn [[cinema scrape!]] flatten
                           (map (fn [movie]
                                  (update movie :screenings #(map (fn [scr]
                                                                    (assoc scr :cinema (name cinema))) %)))
                                (scrape!))))
                    flatten
                    (pmap #(add-movie-metadata movie-db-api-key %))
                    ;; Movies that are not in "The Movie Database" will be dropped here
                    (remove #(empty? (:id %))))
        screenings (mapcat :screenings movies)]
    (jdbc/with-transaction [tx db]
                           (insert-movies! tx (map #(dissoc % :screenings) movies))
                           (remove-screenings-for-movies! tx (map :id movies))
                           (insert-screenings! tx screenings))))

(comment
  (def config (var-get (requiring-resolve 'ov-movies.config/config)))
  (def ds (get-in config [:database :connection-uri]))
  (def movie-db-api-key (get-in config [:movie-db :api-key]))
  (try (time (crawl! ds movie-db-api-key))
       (catch Exception e (println e))))