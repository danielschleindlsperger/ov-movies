(ns ov_movies.crawler.notification
  (:require [ov_movies.util :refer [fetch-sm-secret find-first]]
            [ov_movies.db.db :as db]
            [ov_movies.config :refer [cfg]]
            [clj-http.client :as client]
            [clojure.string :as str])
  (:import [java.time.format DateTimeFormatter]))

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

(def formatter (DateTimeFormatter/ofPattern "E dd.LL. HH:mm"))
(defn format-date [date] (.format date formatter))
(defn format-screening [screening] (-> screening :date format-date))
(defn format-screenings [screenings] (str/join "\n" (map format-screening (sort-by :date screenings))))

(defn format-message [movies-with-screenings]
  (str/join "\n\n" (map (fn [movie]
                          (let [screenings (format-screenings (:screenings movie))
                                link (format "<a href=\"%s/blacklist/%s\">IGNORE</a>" (:api-url cfg) (:id movie))]
                            (str/join "\n" [(str "<b>" (:title movie) ":</b>")
                                            screenings
                                            link]))) movies-with-screenings)))

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