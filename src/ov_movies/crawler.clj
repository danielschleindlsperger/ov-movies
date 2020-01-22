(ns ov_movies.crawler
  (:require
    [ov_movies.util :as u]
    [ov_movies.db.movies :as movies]
    [ov_movies.db.screenings :as screenings]))

(def db
  {:subprotocol "postgres"
   :subname     "//localhost/ov_movies"
   :user        "root"
   :password    "root"})

(defn make-film [] {:id (str (rand-int 1000000))
                    :name (rand-nth ["Die Hard" "The Great Gatsby"])
                    :poster-url nil})

(movies/insert-movie db die-hard)

(def films (map vals [(make-film) (make-film)]))

(defn make-screening [] {:id (str (rand-int 1000000))
                         :movie_id "lalal"
                         :date (u/parse-date "2020-01-01-20-00")})

(movies/insert-movies db {:movies films})

(def ss (screenings/insert-screenings db {:screenings (map vals [(make-screening) (make-screening)])}))

(str (:date (first ss)))

;;; TODO
;; - crawl websites
;; - upsert into database, getting inserted back
;; - notification with newly inserted entries
(defn -main [] (println "crawling..."))