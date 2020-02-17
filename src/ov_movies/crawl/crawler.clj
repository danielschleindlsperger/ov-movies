(ns ov-movies.crawl.crawler
  (:require
    [ov-movies.crawl.scrape :refer [scrape!]]
    [ov-movies.crawl.notification :refer [notify!]]
    [ov-movies.db.db :as db]
    [ov-movies.db.movies :as movies]
    [ov-movies.db.screenings :as screenings]))

(defn screening-tuple [{id       :id
                        movie-id :movie_id
                        date     :date}] (vec [id movie-id date]))

(defn movie-tuple [{id     :id
                    title  :title
                    poster :poster}] (vec [id title poster]))

(defn crawl []
  (let [{movies     :movies
         screenings :screenings} (scrape!)
        inserted-movies (movies/insert-movies db/db-conn {:movies (map movie-tuple movies)})
        inserted-screenings (screenings/insert-screenings db/db-conn {:screenings (map screening-tuple screenings)})]
    (println "inserted" (count inserted-movies) "new movies")
    (println "inserted" (count inserted-screenings) "new screenings")
    (let [upcoming-screenings (db/upcoming-screenings)]
      (println "sending notifications...")
      (notify! upcoming-screenings))))

;; Method described here works as well: https://bernhardwenzel.com/articles/using-clojure-with-aws-lambda/#requesthandler-clojure-version
;(deflambdafn ov_movies.crawler.handler
;             [in out ctx]
;             (println "crawling...")
;             (crawl))