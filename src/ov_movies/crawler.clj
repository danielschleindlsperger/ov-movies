(ns ov_movies.crawler
  (:require
    [ov_movies.util :as u]
    [ov-movies.scrape :as scrape]
    [ov_movies.db.movies :as movies]
    [ov_movies.db.screenings :as screenings]))

(def db
  {:subprotocol "postgres"
   :subname     "//localhost/ov_movies"
   :user        "root"
   :password    "root"})

(defn screening-tuple [{id       :id
                        movie-id :movie-id
                        date     :date}] (vec [id movie-id date]))

(defn movie-tuple [{id :id
                   title :title
                    poster-url :poster-url}] (vec [id title poster-url]))

;;; TODO
;; - notification with newly inserted entries
(defn -main []
  (println "crawling...")
  (let [{movies     :movies
         screenings :screenings} (scrape/scrape!)
        inserted-movies (movies/insert-movies db {:movies (map movie-tuple movies)})
        inserted-screenings (screenings/insert-screenings db {:screenings (map screening-tuple screenings)})]
    (println "inserted" (count inserted-movies) "new movies")
    (println "inserted" (count inserted-screenings) "new screenings")))
