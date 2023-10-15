(ns ov-movies.handlers.crawl
  (:require [taoensso.timbre :as log]
            [ov-movies.crawl.crawler :refer [crawl!]]
            [ov-movies.handlers.util :refer [ok]]))

(defn crawl-handler
  [{:keys [db movie-db-api-key passphrase query-params]}]
  (let [supplied-passphrase (get query-params "passphrase")]
    (if (= supplied-passphrase passphrase)
      (do (log/info "Starting to crawl...")
          (crawl! db movie-db-api-key)
          (ok "Crawled movies successfully!"))
      {:status 401,
       :body "invalid passphrase provided.",
       :headers {"content-type" "text/plain"}})))
