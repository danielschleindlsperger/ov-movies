(ns ov_movies.crawler
  (:require
    [ov_movies.util :as u]
    [ov-movies.scrape :as scrape]
    [ov_movies.db.db :refer [db-conn]]
    [ov_movies.db.movies :as movies]
    [ov_movies.db.screenings :as screenings]
    [uswitch.lambada.core :refer [deflambdafn]]))

(defn screening-tuple [{id       :id
                        movie-id :movie-id
                        date     :date}] (vec [id movie-id date]))

(defn movie-tuple [{id         :id
                    title      :title
                    poster-url :poster-url}] (vec [id title poster-url]))

(defn crawl []
  (let [{movies     :movies
         screenings :screenings} (scrape/scrape!)
        inserted-movies (movies/insert-movies db-conn {:movies (map movie-tuple movies)})
        inserted-screenings (screenings/insert-screenings db-conn {:screenings (map screening-tuple screenings)})]
    (println "inserted" (count inserted-movies) "new movies")
    (println "inserted" (count inserted-screenings) "new screenings")))

;;; TODO
;; - notification (https://pushover.net/) with newly inserted entries

;; Method described here works as well: https://bernhardwenzel.com/articles/using-clojure-with-aws-lambda/#requesthandler-clojure-version
(deflambdafn ov_movies.crawler
             [in out ctx]
             (println "crawling...")
             (crawl))