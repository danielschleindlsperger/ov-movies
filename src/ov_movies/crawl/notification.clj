(ns ov-movies.crawl.notification
  (:require [ov-movies.config :refer [config]]
            [org.httpkit.client :as client]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

;; https://pushover.net/api
(def endpoint "https://api.pushover.net/1/messages.json")

(defn format-message
  [movies]
  (str/join "\n\n"
            (map (fn [movie]
                   (str/join "\n"
                             [(str "<b>" (:title movie) ":</b>")
                              (str/join "\n"
                                        (distinct (map :cinema
                                                    (:screenings movie))))]))
              movies)))

(defn send-message
  [params api-key user-key]
  {:pre [(some? api-key) (some? user-key)]}
  (let [form-params (merge params {:token api-key, :user user-key})]
    (deref (client/request {:url endpoint,
                            :method :post,
                            :headers {"accept" "application/json"},
                            :form-params form-params}))))

(defn send-message-mock
  [params _api-key _user-key]
  (log/debug "SENDING NOTIFICATION")
  (log/debug params)
  {:status 200})

(defn notify!
  [movies send-message]
  (let [should-send? (pos? (count movies))
        message (format-message movies)
        params {:title "Originale!",
                :message message,
                :html 1,
                :url (-> config
                         :server
                         :base-url)}]
    (when should-send? (send-message params))))