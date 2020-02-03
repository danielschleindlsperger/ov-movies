(ns ov_movies.notification
  (:require [ov_movies.util :refer [fetch-sm-secret find-first]]
            [ov_movies.db.db :as db]
            [ov_movies.config :refer [cfg]]
            [clj-http.client :as client]
            [clojure.string :as str]))

;; POST to this
;; https://pushover.net/api
(def endpoint "https://api.pushover.net/1/messages.json")

(def api-token
  "API token that identifies this application with Pushover."
  (let [secret-id (:pushover-api-key-secret-id cfg)]
    (fetch-sm-secret secret-id)))

(def user-key
  "Pushover user key to identify which user to send notifications to."
  (let [secret-id (:pushover-user-key-secret-id cfg)]
    (fetch-sm-secret secret-id)))

(defn group-by-movie [screenings]
  (reduce-kv (fn [movies movie screenings]
               (conj movies (assoc movie :screenings (map #(dissoc % :movie) screenings))))
             []
             (group-by :movie screenings)))

(defn format-screenings [screenings] (str/join "\n" (map (fn [s] (str (:date s))) (sort-by :date screenings))))

;;; TODO: format date "Tue, 23rd Jan, 23:00"
(defn format-message [movies-with-screenings]
  (str/join "\n\n" (map (fn [movie]
                          (let [screenings (format-screenings (:screenings movie))]
                            (str/join "\n" [(str "<b>" (:title movie) ":</b>")
                                            screenings
                                            "<a href=\"http://example.com/\">BLACKLIST MOVIE</a>"]))) movies-with-screenings)))

(defn notify! [upcoming-screenings]
  (let [should-send? (< 0 (count upcoming-screenings))
        movies-with-screenings (group-by-movie upcoming-screenings)
        message (format-message movies-with-screenings)
        params {:token   api-token
                :user    user-key
                :title   "Originale!"
                :message message
                :html    1
                ;; URL hardcoded until we build a custom page
                :url     "https://www.cineplex.de/filmreihe/original/548/neufahrn/"}]
    (when should-send? (client/post endpoint {:form-params params
                                              :accept      :json}))))