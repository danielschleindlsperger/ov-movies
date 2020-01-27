(ns ov_movies.crawler
  (:require
    [ov_movies.scrape :refer [scrape!]]
    [ov_movies.notification :refer [notify!]]
    [ov_movies.db.db :refer [db-conn]]
    [ov_movies.db.movies :as movies]
    [ov_movies.db.screenings :as screenings]
    [uswitch.lambada.core :refer [deflambdafn]]))

(defn screening-tuple [{id       :id
                        movie-id :movie_id
                        date     :date}] (vec [id movie-id date]))

(defn movie-tuple [{id     :id
                    title  :title
                    poster :poster}] (vec [id title poster]))

(defn crawl []
  (let [{movies     :movies
         screenings :screenings} (scrape!)
        inserted-movies (movies/insert-movies db-conn {:movies (map movie-tuple movies)})
        inserted-screenings (screenings/insert-screenings db-conn {:screenings (map screening-tuple screenings)})]
    (println "inserted" (count inserted-movies) "new movies")
    (println "inserted" (count inserted-screenings) "new screenings")
    (println "sending notifications...")
    (notify! inserted-screenings movies)))

;; Method described here works as well: https://bernhardwenzel.com/articles/using-clojure-with-aws-lambda/#requesthandler-clojure-version
(deflambdafn ov_movies.crawler
             [in out ctx]
             (println "crawling...")
             (crawl))