(ns ov-movies.handlers
  (:require [reitit.ring :as ring]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [ov-movies.util :refer [offset-date-time->iso-offset-date-time-string format-date]]
            [ov-movies.crawl.crawler :refer [crawl!]]
            [ov-movies.crawl.notification :refer [notify! send-message]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings blacklist-movie!]]
            [ov-movies.database :refer [db]]
            [ov-movies.config :refer [config]])
  (:import [java.time OffsetDateTime]
           [java.io PrintWriter]))

(extend OffsetDateTime json/JSONWriter {:-write (fn [in ^PrintWriter out]
                                                  (.print out (str "\"" (offset-date-time->iso-offset-date-time-string in) "\"")))})

;; HTTP helpers

(defn not-found
  ([] {:status 404 :headers {"content-type" "text/plain"} :body "not found."})
  ([body] {:stauts 404 :headers {"content-type" "text/plain"} :body body}))

(defn server-error
  ([] {:status 500 :headers {"content-type" "text/plain"} :body "internal server error."})
  ([body] {:status 500 :headers {"content-type" "text/plain"} :body body}))

(defn ok
  ([body] {:status 200 :headers {"content-type" "text/plain"} :body body})
  ([body headers] {:status 200 :headers (merge {"content-type" "text/plain"} headers) :body body}))

(defn temporary-redirect [location] {:status 307 :headers {"Location" location}})

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

(defn blacklist-handler [{:keys [db path-params headers] :as req}]
  (let [id (:id path-params)
        result (first (blacklist-movie! db {:id id}))
        referer (get headers "referer")]
    (if (empty? result)
      (not-found (format "Movie with id '%s' not found." id))
      (do
        (log/info {:msg "successfully blacklisted movie" :movie result})
        (if (nil? referer)
          (ok (format "Successfully blacklisted movie '%s'" (:title result)))
          (temporary-redirect referer))))))

(defn crawl-handler [{:keys [db send-message]}]
  (do (log/info "Starting to crawl...")
      (crawl! db)
      (let [upcoming-movies (get-movies-with-upcoming-screenings db)]
        (log/info "sending notifications...")
        (let [res (notify! upcoming-movies send-message)]
          (if (<= 300 (:status res))
            (do (log/error (:body res))
                (server-error "something went wrong sending out the notifications"))
            (ok "Crawled movies successfully!"))))))

(defn render-upcoming-movies [upcoming-movies base-url]
  (html5 {:lang "en"}
         [:head
          [:title "Upcoming Movies - ov-movies"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.max-w-2xl.p-4.mx-auto
           [:h1.text-3xl.text-center.font-bold "Upcoming Movies"]
           [:div.mt-12
            (for [movie upcoming-movies]
              [:section.mt-12
               [:img {:src (:poster movie) :alt (:title movie) :style "max-width: 300px;"}]
               [:h1.mt-6.text-2xl.font-bold (:title movie)]
               [:ul.font-mono.mt-2
                (for [screening (:screenings movie)]
                  [:li.mt-2 (format-date (:date screening))])]
               [:a.px-4.py-2.mt-4.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md
                {:href (str base-url "/blacklist/" (:id movie))} "Blacklist"]])]]]))

(defn upcoming-screenings-handler [{:keys [db]}]
  (let [upcoming-movies (get-movies-with-upcoming-screenings db)
        base-url (-> config :server :base-url)]
    (ok (render-upcoming-movies upcoming-movies base-url) {"content-type" "text/html"})))

(defn create-handler [config]
  (ring/ring-handler (ring/router [["/crawl" {:get {:middleware [[wrap-db db] [wrap-message-sender config]]
                                                    :handler    crawl-handler}}]
                                   ["/blacklist/:id" {:get {:middleware [[wrap-db db]]
                                                            :handler    blacklist-handler}}]
                                   ["/" {:get {:middleware [[wrap-db db]]
                                               :handler    upcoming-screenings-handler}}]])))