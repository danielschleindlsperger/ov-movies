(ns ov-movies.crawl.crawler
  (:require [next.jdbc :as jdbc]
            [ov-movies.movie :refer [insert-movies!]]
            [ov-movies.screening :refer [insert-screenings! remove-screenings-for-movies!]]
            [ov-movies.crawl.scrapers.cineplex-neufahrn :as cineplex-neufahrn]
            [ov-movies.crawl.scrapers.cineplex-germering :as cineplex-germering]
            [ov-movies.crawl.scrapers.cadillac-veranda :as cadillac-veranda]
            [ov-movies.crawl.scrapers.rio-filmpalast :as rio-filmpalast]
            [ov-movies.movie-api :as movie-api]))

(defn- add-movie-metadata [api-key movie]
  (let [meta-data (movie-api/search-movie api-key (:title movie))
        id (str (:id meta-data))
        screenings (map #(assoc % :movie-id id) (:screenings movie))]
    (merge movie {:id            id
                  :title         (:title meta-data)
                  ;:original-title (:original-title meta-data)
                  :original-lang (:original-language meta-data)
                  :poster        (str "https://image.tmdb.org/t/p/w500" (:poster-path meta-data))
                  :description   (:overview meta-data)
                  :screenings    screenings})))

(def cinemas [[:cineplex-germering cineplex-germering/scrape!]
              [:cineplex-neufahrn cineplex-neufahrn/scrape!]
              [:cadillac-veranda cadillac-veranda/scrape!]
              [:rio-filmpalast rio-filmpalast/scrape!]])

(defn- add-cinema [movie cinema]
  (update movie :screenings (fn [screenings]
                              (map #(assoc % :cinema (name cinema))
                                   screenings))))

(defn- merge-by [compare-fn merge-fn coll]
  (let [groups (group-by compare-fn coll)]
    (map (fn [[_k vals]] (merge-fn vals)) groups)))

(defn- merge-same-movies [movies]
  (merge-by :id
            (partial reduce
                     (fn [acc movie]
                       (merge acc
                              movie
                              {:screenings (concat (get acc :screenings [])
                                                   (get movie :screenings []))}))
                     {})
            movies))

(defn crawl! [db movie-db-api-key]
  (let [movies (->> cinemas
                    (mapcat (fn [[cinema scrape!]]
                              (map #(add-cinema % cinema)
                                   (scrape!))))
                    (pmap #(add-movie-metadata movie-db-api-key %))
                    (merge-same-movies)
                    ;; Movies that are not in "The Movie Database" will be dropped here
                    (remove #(empty? (:id %))))
        screenings (mapcat :screenings movies)]
    (jdbc/with-transaction [tx db]
                           (insert-movies! tx (map #(dissoc % :screenings) movies))
                           (remove-screenings-for-movies! tx (map :id movies))
                           (insert-screenings! tx screenings))
    :success))

(comment
  (def config (var-get (requiring-resolve 'ov-movies.config/config)))
  (def ds (get-in config [:database :connection-uri]))
  (def movie-db-api-key (get-in config [:movie-db :api-key]))
  (crawl! ds movie-db-api-key))