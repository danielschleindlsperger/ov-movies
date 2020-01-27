(ns ov_movies.notification
  (:require [ov_movies.util :refer [fetch-sm-secret find-first]]
            [ov_movies.config :refer [cfg]]
            [clojure.string :refer [join]]
            [clj-http.client :as client]))

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

(defn screenings-for-movies [screenings movies]
  (let [all-movies (map (fn [movie]
                          (let [xs (filter (fn [s] (= (:movie-id s) (:id movie))) screenings)]
                            (assoc movie :screenings xs))) movies)]
    (filter (fn [movie] (< 0 (count (:screenings movie)))) all-movies)))

(defn format-message [movies-with-screenings]
  (join "\n" (map (fn [movie]
                    (str (:title movie) ": " (join ", " (map :date (:screenings movie))))) movies-with-screenings)))

(format-message [{:title "huiiboo" :screenings [{:date "2020-10-10"} {:date "2020-10-10"}]}
                 {:title "another one!" :screenings [{:date "2020-10-10"} {:date "2020-10-10"}]}])

(defn notify! [new-screenings movies]
  (let [should-send? (< 0 (count new-screenings))
        movies-with-screenings (screenings-for-movies new-screenings movies)
        message (format-message movies-with-screenings)
        params {:token   api-token
                :user    user-key
                :title   "New OV movies!"
                :message message
                ;; URL hardcoded until we build a custom page
                :url     "https://www.cineplex.de/filmreihe/original/548/neufahrn/"}]
    (when should-send? (client/post endpoint {:form-params params
                                              :accept      :json}))))