(ns ov-movies.crawl.crawler
  (:require [next.jdbc :as jdbc]
            [ov-movies.movie :refer [insert-movies!]]
            [ov-movies.screening :refer [insert-screenings!]]
            [ov-movies.crawl.scrapers.cineplex-neufahrn :as cineplex-neufahrn]
            [ov-movies.crawl.scrapers.cineplex-germering :as cineplex-germering]
            [ov-movies.movie-api :as movie-api]
            [taoensso.timbre :as log]))

(defn- add-movie-metadata [api-key movie]
  (let [meta-data (movie-api/search-movie api-key (:title movie))
        original-lang (:original-language meta-data)
        id (:id meta-data)
        screenings (map #(assoc % :movie-id id) (:screenings movie))]
    (assoc movie :original-lang original-lang :id id :screenings screenings)))

(def ^:private cinemas [[:cineplex-germering cineplex-germering/scrape!]
                        [:cineplex-neufahrn cineplex-neufahrn/scrape!]])

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
                    (filter :id))
        screenings (mapcat :screenings movies)]
    (jdbc/with-transaction [tx db]
      (let [new-movies (insert-movies! tx (map #(dissoc % :screenings) movies))
            new-screenings (insert-screenings! tx screenings)]
        (log/info "inserted" (count new-movies) "new movies")
        (log/info "inserted" (count new-screenings) "new screenings")))))

(comment
  (def config (var-get (requiring-resolve 'ov-movies.config/config)))
  (def ds (get-in config [:database :connection-uri]))
  (def movie-db-api-key (get-in config [:movie-db :api-key]))
  (try (time (crawl! ds movie-db-api-key))
       (catch Exception e (println e))))