(ns ov-movies.handlers.upcoming-movies
  (:require [hiccup.page :refer [html5]]
            [ov-movies.config :refer [config]]
            [ov-movies.util :refer [format-date]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings]]
            [ov-movies.handlers.util :refer [ok]])
  (:import [java.net URLEncoder]
           [java.nio.charset StandardCharsets]))

(defn url-encode
  [s]
  {:pre [(string? s)]}
  (URLEncoder/encode s (str StandardCharsets/UTF_8)))

(defn- render-upcoming-movies [upcoming-movies base-url]
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
            (when (empty? upcoming-movies) [:section.mt-12.flex.justify-center
                                            [:span "No upcoming movies."]])
            (for [movie upcoming-movies]
              [:section.mt-12
               [:img {:src (:poster movie) :alt (:title movie) :loading "lazy" :style "max-width: 300px;"}]
               [:h1.mt-6.text-2xl.font-bold (:title movie)]
               [:p.mt-2 (:description movie)]
               [:a.px-4.py-2.mt-4.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md
                {:href (str "https://duckduckgo.com/?q=imdb+" (url-encode (:title movie)))}
                "Research movie"]
               [:h2.mt-8.text-xl.font-bold "Dates"]
               [:ul.font-mono.mt-4
                (for [screening (:screenings movie)]
                  [:li.mt-2 (format-date (:date screening))])]
               [:a.px-4.py-2.mt-4.inline-block.bg-red-700.text-gray-100.font-semibold.rounded.shadow-md
                {:href (str base-url "/blacklist/" (:id movie))} "Block movie"]])]]]))

(defn upcoming-movies-handler [{:keys [db]}]
  (let [upcoming-movies (get-movies-with-upcoming-screenings db)
        base-url (-> config :server :base-url)]
    (ok (render-upcoming-movies upcoming-movies base-url) {"content-type" "text/html"})))

