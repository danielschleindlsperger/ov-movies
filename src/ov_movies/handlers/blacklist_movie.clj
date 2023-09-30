(ns ov-movies.handlers.blacklist-movie
  (:require [ov-movies.handlers.util :refer [ok not-found temporary-redirect]]
            [ov-movies.movie :refer [blacklist-movie!]]
            [taoensso.timbre :as log]))

(defn blacklist-movie-handler
  [{:keys [db path-params headers]}]
  (let [id (:id path-params)
        result (first (blacklist-movie! db {:id id}))
        referer (get headers "referer")]
    (if (empty? result)
      (not-found (format "Movie with id '%s' not found." id))
      (do (log/info {:msg "successfully blacklisted movie", :movie result})
          (if (nil? referer)
            (ok (format "Successfully blacklisted movie '%s'" (:title result)))
            (temporary-redirect referer))))))
