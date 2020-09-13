(ns ov-movies.handlers.create-handler
  (:require [reitit.ring :as ring]
            [clojure.string :as str]
            [ring.middleware.params :as params]
            [ov-movies.database :refer [db]]
            [ov-movies.handlers.crawl :refer [crawl-handler]]
            [ov-movies.handlers.upcoming-movies :refer [upcoming-movies-handler]]
            [ov-movies.handlers.blacklist-movie :refer [blacklist-movie-handler]]
            [ov-movies.crawl.notification :refer [send-message send-message-mock]])
  (:import [java.util Base64]))

;; Middleware

(defn wrap-db [handler db]
  (fn [req]
    (handler (assoc req :db db))))

(defn wrap-message-sender [handler config]
  (let [api-key (-> config :pushover :api-key)
        user-key (-> config :pushover :user-key)
        send (if (= "dev" (:env config)) send-message-mock send-message)]
    (fn [req]
      (handler (assoc req :send-message (fn [params] (send params api-key user-key)))))))

(defn wrap-movie-db [handler config]
  (let [api-key (-> config :movie-db :api-key)]
    (fn [req]
      (handler (assoc req :movie-db-api-key api-key)))))

(defn wrap-passphrase [handler passphrase]
  (fn [req]
    (handler (assoc req :passphrase passphrase))))

(defn decode-base64 [s]
  (String. (.decode (Base64/getDecoder) s)))
(defn parse-password [auth-header]
  (when (string? auth-header)
    (-> auth-header
        (str/replace "Basic " "")
        (decode-base64)
        (str/split #":")
        (nth 1 nil))))
(defn wrap-basic-auth [handler password]
  {:pre [(not (str/blank? password))]}
  (fn [req]
    (let [supplied-password (-> req :headers (get "authorization") parse-password)]
      (if (= supplied-password password)
        (handler req)
        {:status  401
         :headers {"WWW-Authenticate" "Basic realm=\"Allow triggering a crawl\""
                   "content-type"     "text/basic"}
         :body    "Please enter the correct password. You can omit the username."}))))

;; Combine handlers

(defn create-handler [config]
  (ring/ring-handler (ring/router [["/crawl" {:get {:middleware [[params/wrap-params]
                                                                 [wrap-passphrase (:passphrase config)]
                                                                 [wrap-db db]
                                                                 [wrap-message-sender config]
                                                                 [wrap-movie-db config]]
                                                    :handler    crawl-handler}}]
                                   ["/blacklist/:id" {:get {:middleware [[wrap-basic-auth (:passphrase config)] [wrap-db db]]
                                                            :handler    blacklist-movie-handler}}]
                                   ["/" {:get {:middleware [[wrap-db db]]
                                               :handler    upcoming-movies-handler}}]])))