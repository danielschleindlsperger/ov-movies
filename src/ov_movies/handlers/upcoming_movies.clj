(ns ov-movies.handlers.upcoming-movies
  (:require [clojure.string :as str]
            [hiccup.page :refer [html5]]
            [ov-movies.config :refer [config]]
            [ov-movies.util :refer [format-date]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings]]
            [ov-movies.handlers.util :refer [ok]]
            [ov-movies.crawl.crawler :refer [cinemas]])
  (:import [java.net URLEncoder]
           [java.nio.charset StandardCharsets]
           [java.util Locale]))

(defn url-encode
  [s]
  {:pre [(string? s)]}
  (URLEncoder/encode s (str StandardCharsets/UTF_8)))

(defn- locale [lang]
  (let [locale (Locale. lang)]
    (.getDisplayLanguage locale locale)))

(defn- fmt-cinema [cinema] (->> (str/split cinema #"-")
                                (map str/capitalize)
                                (str/join " ")))

(defn- ensure-vec [x] (-> x vector flatten))

(defn- checkbox-multi-select [name options selected-values]
  (let [selected? (fn [x] (contains? (set (ensure-vec selected-values)) x))]
    [:fieldset.flex.flex-col
     (for [{:keys [value display-name]} options]
       [:label
        [:input {:type "checkbox" :name name :value value :checked (selected? value)} display-name]])]))

(defn- radio-multi-select [name options selected-value]
  [:fieldset.flex.flex-col
   (for [{:keys [value display-name]} options]
     [:label
      [:input {:type "radio" :name name :value value :checked (= value selected-value)} display-name]])])

(def default-form-state {"language" "non-dubbed"
                         "cinema" (map (comp name first) cinemas)})

(def ^:private cinema-options
  (map (fn [c]
         (let [cinema (first c)]
           {:value (name cinema)
            :display-name (fmt-cinema (name cinema))})) cinemas))

(def ^:private language-options
  [{:value "non-dubbed"
    :display-name "Non-dubbed (Deutsche und originale)"}
   {:value "originals-only"
    :display-name "Originals only"}
   {:value "all"
    :display-name "Alle"}])

(defn- filter-form [form-state]
  [:form.flex.justify-between.top-0.sticky.bg-white.py-4.shadow-lg
   (checkbox-multi-select "cinema" cinema-options (get form-state "cinema"))
   (radio-multi-select "language" language-options (get form-state "language"))
   [:section
    [:a.px-4.py-2.ml-4.inline-block.text-gray-800.border.font-semibold.rounded.shadow-md {:href "/"} "Reset"]
    [:button.px-4.py-2.ml-4.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md "Filter"]]])

(defn- render-upcoming-movies [upcoming-movies base-url form-state]
  (html5 {:lang "en"}
         [:head
          [:title "Upcoming Movies - ov-movies"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.max-w-2xl.p-4.mx-auto
           [:h1.text-3xl.text-center.font-bold "Upcoming Movies"]
           (filter-form form-state)
           [:div.mt-12
            (when (empty? upcoming-movies) [:section.mt-12.flex.justify-center
                                            [:span "No upcoming movies."]])
            (for [movie upcoming-movies]
              [:section.mt-12
               [:img {:src (:poster movie) :alt (:title movie) :loading "lazy" :style "max-width: 300px;"}]
               [:h1.text-2xl.font-bold.mt-6 (:title movie)]
               [:div.mt-2 "Originalsprache: " (locale (:original-lang movie))]
               [:p.mt-2 (:description movie)]
               (for [[cinema screenings] (group-by :cinema (:screenings movie))]
                 [:section
                  [:h1.mt-8.text-xl.font-bold (fmt-cinema cinema)]
                  [:ul.font-mono.mt-4
                   (for [screening screenings]
                     [:li.mt-2
                      (format-date (:date screening))
                      (when (:original? screening) [:div.inline-block.bg-red-800.font-bold.text-gray-100.ml-2.px-1.text-xs.rounded.whitespace-no-wrap.uppercase "Originalversion"])])]])
               [:div.flex
                [:a.px-4.py-2.mt-4.inline-block.bg-red-700.text-gray-100.font-semibold.rounded.shadow-md
                 {:href (str base-url "/blacklist/" (:id movie))} "Hide movie forever"]
                [:a.px-4.py-2.mt-4.ml-4.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md
                 {:href (str "https://duckduckgo.com/?q=imdb+" (url-encode (:title movie)))}
                 "Research movie"]]])]]]))

;; TODO: validate query params
(defn upcoming-movies-handler [{:keys [db query-params]}]
  (let [upcoming-movies (get-movies-with-upcoming-screenings db)
        base-url (-> config :server :base-url)
        form-state (if (empty? query-params) default-form-state query-params)]
    (ok (render-upcoming-movies upcoming-movies base-url form-state) {"content-type" "text/html"})))

