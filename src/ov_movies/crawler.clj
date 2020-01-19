(ns ov_movies.crawler
  (:require
    [ov_movies.db.movies :as movies]))

(def db
  {:subprotocol "postgres"
   :subname "//localhost/ov_movies"
   :user "root"
   :password "root"})

(movies/insert-movie db {:id "asdfsfd" :name "Die Hard" :poster-url nil})

;;; TODO
;; - crawl websites
;; - upsert into database, getting inserted back
;; - notification with newly inserted entries
(defn -main [] (println "crawling..."))