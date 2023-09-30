(ns ov-movies.handlers.crawl
  (:require [taoensso.timbre :as log]
            [ov-movies.crawl.crawler :refer [crawl!]]
            [ov-movies.crawl.notification :refer [notify!]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings]]
            [ov-movies.handlers.util :refer [ok server-error]]))

(defn crawl-handler
  [{:keys [db movie-db-api-key send-message passphrase query-params]}]
  (let [supplied-passphrase (get query-params "passphrase")]
    (if (= supplied-passphrase passphrase)
      (do (log/info "Starting to crawl...")
          (crawl! db movie-db-api-key)
          (let [upcoming-movies (get-movies-with-upcoming-screenings db)]
            (log/info "sending notifications...")
            (let [res (notify! upcoming-movies send-message)]
              (if (and (some? res) (<= 300 (:status res)))
                (do (log/error (:body res))
                    (server-error
                      "something went wrong sending out the notifications"))
                (ok "Crawled movies successfully!")))))
      {:status 401,
       :body "invalid passphrase provided.",
       :headers {"content-type" "text/plain"}})))
