(ns ov-movies.crawl.notification
  (:require [ov-movies.util :refer [find-first]]
            [ov-movies.config :refer [config]]
    ;; TODO: replace with http-kit/client
            [clj-http.client :as client]
            [clojure.string :as str]
            [taoensso.timbre :as log])
  (:import [java.time.format DateTimeFormatter]))

;; https://pushover.net/api
(def endpoint "https://api.pushover.net/1/messages.json")

(def formatter (DateTimeFormatter/ofPattern "E dd.LL. HH:mm"))
(defn format-date [date] (.format date formatter))
(defn format-screening [screening] (-> screening :date format-date))
(defn format-screenings [screenings] (str/join "\n" (map format-screening (sort-by :date screenings))))

(defn format-message [movies]
  (str/join "\n\n" (map (fn [movie]
                          (let [screenings (format-screenings (:screenings movie))
                                link (format "<a href=\"%s/blacklist/%s\">IGNORE</a>" (-> config :server :base-url) (:id movie))]
                            (str/join "\n" [(str "<b>" (:title movie) ":</b>")
                                            screenings
                                            link]))) movies)))

(defn send-message [params api-key user-key]
  {:pre [(some? api-key) (some? user-key)]}
  (let [form-params (merge params {:token api-key :user user-key})]
    (client/post endpoint {:form-params form-params
                           :accept      :json})))

(defn send-message-dev [params api-key user-key]
  (log/debug "SENDING NOTIFICATION")
  (log/debug (clojure.pprint/pprint params)))

(defn notify! [movies send-message]
  (let [should-send? (< 0 (count movies))
        message (format-message movies)
        params {:title   "Originale!"
                :message message
                :html    1
                ;; URL hardcoded until we build a custom page
                :url     "https://www.cineplex.de/filmreihe/original/548/neufahrn/"}]
    (when should-send? (send-message params))))