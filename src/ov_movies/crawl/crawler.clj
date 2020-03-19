(ns ov-movies.crawl.crawler
  (:require
   [ov-movies.crawl.scrape :refer [scrape!]]
   [ov-movies.crawl.notification :refer [notify!]]
   [ov-movies.movie :refer [insert-movies! get-movies-with-upcoming-screenings]]
   [ov-movies.screening :refer [insert-screenings!]]
   [taoensso.timbre :as log]))

(defn crawl! [db]
  (let [{movies     :movies
         screenings :screenings} (scrape!)
        inserted-movies  (when (seq movies) (insert-movies! db movies))
        inserted-screenings (when (seq screenings) (insert-screenings! db screenings))]
    (log/info "inserted" (count inserted-movies) "new movies")
    (log/info "inserted" (count inserted-screenings) "new screenings")))
