(ns ov-movies.handlers
  (:require [reitit.ring :as ring]
            [taoensso.timbre :as log]
            [ov-movies.crawl.crawler :refer [crawl!]]
            [ov-movies.crawl.notification :refer [notify! send-message]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings blacklist-movie!]]
            [ov-movies.database :refer [db]]))

;; HTTP helpers

(defn not-found
  ([] {:headers {"status" 404 "content-type" "text/plain"} :body "not found."})
  ([body] {:headers {"status" 404 "content-type" "text/plain"} :body body}))

(defn ok
  ([body] {:headers {"status" 200 "content-type" "text/plain"} :body body})
  ([body headers] {:headers (merge headers {"status" 200 "content-type" "text/plain"}) :body body}))

;; Middleware

(defn wrap-db [handler db]
  (fn [req]
    (handler (assoc req :db db))))

(defn wrap-message-sender [handler config]
  (let [api-key (-> config :pushover :api-key)
        user-key (-> config :pushover :user-key)]
    (fn [req]
      (handler (assoc req :send-message (fn [params] (send-message params api-key user-key)))))))

;; Handlers

(defn blacklist-handler [{:keys [db path-params] :as req}]
  (let [id (-> path-params :id)
        result (first (blacklist-movie! db {:id id}))]
    (if (empty? result)
      (not-found (format "Movie with id '%s' not found." id))
      (do
        (log/info {:msg "successfully blacklisted movie" :movie result})
        (ok (format "Successfully blacklisted movie '%s'" (:title result)))))))

(defn crawl-handler [{:keys [db send-message]}]
  (do (log/info "Starting to crawl...")
      (crawl! db)
      (let [upcoming-movies (get-movies-with-upcoming-screenings db)]
        (println "sending notifications...")
        (notify! upcoming-movies send-message)
        (ok "Crawled movies successfully!"))))

(defn create-handler [config]
  (ring/ring-handler (ring/router [["/crawl" {:get {:middleware [[wrap-db db] [wrap-message-sender config]]
                                                    :handler    crawl-handler}}]
                                   ["/blacklist/:id" {:get {:middleware [[wrap-db db]]
                                                            :handler    blacklist-handler}}]])))