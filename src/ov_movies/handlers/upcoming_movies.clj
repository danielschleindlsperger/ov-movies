(ns ov-movies.handlers.upcoming-movies
  (:require [clojure.string :as str]
            [hiccup.page :refer [html5]]
            [ov-movies.config :refer [config]]
            [ov-movies.util :refer [format-date]]
            [ov-movies.movie :refer [get-movies-with-upcoming-screenings]]
            [ov-movies.assets :refer [assets]]
            [ov-movies.handlers.util :refer [ok]]
            [ov-movies.crawl.crawler :refer [cinemas]])
  (:import [java.net URLEncoder]
           [java.nio.charset StandardCharsets]
           [java.util Locale]))

(defn- url-encode
  [s]
  {:pre [(string? s)]}
  (URLEncoder/encode s (str StandardCharsets/UTF_8)))

(defn- locale
  [lang]
  (let [locale (Locale. lang)] (.getDisplayLanguage locale locale)))

(defn- fmt-cinema
  [cinema]
  (->> (str/split cinema #"-")
       (map str/capitalize)
       (str/join " ")))

(defn- ensure-vec
  [x]
  (-> x
      vector
      flatten))

(defn- checkbox-multi-select
  [name options selected-values]
  (let [selected? (fn [x] (contains? (set (ensure-vec selected-values)) x))]
    [:fieldset.flex.flex-col.mt-4
     (for [{:keys [value display-name]} options]
       [:label
        [:input.mr-2
         {:type "checkbox",
          :name name,
          :value value,
          :checked (selected? value)} display-name]])]))

(defn- radio-multi-select
  [name options selected-value]
  [:fieldset.flex.flex-col.mt-4
   (for [{:keys [value display-name]} options]
     [:label
      [:input.mr-2
       {:type "radio",
        :name name,
        :value value,
        :checked (= value selected-value)} display-name]])])

(def ^:private caret
  [:svg.w-6.h-6
   {:viewbox "0 0 16 16",
    :class "bi bi-caret-down-fill",
    :fill "currentColor",
    :xmlns "http://www.w3.org/2000/svg"}
   [:path
    {:d
       "M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"}]])

(def ^:private default-form-state
  {"language" "non-dubbed", "cinema" (map (comp name first) cinemas)})

(def ^:private cinema-options
  (map (fn [c]
         (let [cinema (first c)]
           {:value (name cinema), :display-name (fmt-cinema (name cinema))}))
    cinemas))

(def ^:private language-options
  [{:value "non-dubbed", :display-name "Non-dubbed (Deutsche und originale)"}
   {:value "originals-only", :display-name "Originals only"}
   {:value "all", :display-name "Alle"}])

(defn- filter-form
  [form-state]
  [:section.flex.justify-center.top-0.left-0.w-screen.sticky.bg-white.mt-4.p-4.shadow-lg
   {:style "margin-left: -50vw; margin-right: -50vw;", :data-filter-form ""}
   [:form.md:flex.md:justify-between.w-full.max-w-4xl
    (checkbox-multi-select "cinema" cinema-options (get form-state "cinema"))
    (radio-multi-select "language" language-options (get form-state "language"))
    [:section.flex.items-center.mt-4
     [:div.flex.justify-end.w-full
      [:a.px-4.py-2.ml-2.inline-block.text-gray-800.border.font-semibold.rounded.shadow-md
       {:href "/"} "Reset"]
      [:button.px-4.py-2.ml-2.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md
       "Filter"]]]]])

(defn- render-upcoming-movies
  [upcoming-movies base-url form-state]
  (html5
    {:lang "en"}
    [:head [:title "Upcoming Movies - ov-movies"] [:meta {:charset "UTF-8"}]
     [:meta
      {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
     [:link
      {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css",
       :rel "stylesheet"}]]
    [:script (:js assets)]
    [:body
     [:main.max-w-2xl.p-4.mx-auto
      [:h1.text-4xl.text-center.font-bold "Upcoming Movies"]
      [:div.flex.justify-center
       [:button.p-4.transition-transform.origin-center.transform.duration-200
        {:data-mobile-form-toggle "", :aria-label "Toggle movie filter form"}
        caret]] (filter-form form-state)
      [:div.mt-12
       (when (empty? upcoming-movies)
         [:section.mt-12.flex.justify-center [:span "No upcoming movies."]])
       (for [movie upcoming-movies]
         [:section.mt-12
          [:img
           {:src (:poster movie),
            :alt (:title movie),
            :loading "lazy",
            :style "max-width: 300px;"}]
          [:h1.text-2xl.font-bold.mt-6 (:title movie)]
          [:div.mt-2 "Originalsprache: " (locale (:original-lang movie))]
          [:p.mt-2 (:description movie)]
          (for [[cinema screenings] (group-by :cinema (:screenings movie))]
            [:section [:h1.mt-8.text-xl.font-bold (fmt-cinema cinema)]
             [:ul.font-mono.mt-4
              (for [screening screenings]
                [:li.mt-2 (format-date (:date screening))
                 (when (:original? screening)
                   [:div.inline-block.bg-red-800.font-bold.text-gray-100.ml-2.px-1.text-xs.rounded.whitespace-no-wrap.uppercase
                    "Originalversion"])])]])
          [:div.flex
           [:a.px-4.py-2.mt-4.inline-block.bg-red-700.text-gray-100.font-semibold.rounded.shadow-md
            {:href (str base-url "/blacklist/" (:id movie))}
            "Hide movie forever"]
           [:a.px-4.py-2.mt-4.ml-4.inline-block.bg-gray-800.text-gray-100.font-semibold.rounded.shadow-md
            {:href (str "https://duckduckgo.com/?q=imdb+"
                        (url-encode (:title movie)))} "Research movie"]]])]]]))

(defn- filter-movies
  [movies form-state]
  (let [lang (get form-state "language")
        in-selected-location? (fn [_movie screening]
                                (contains? (set (ensure-vec (get form-state
                                                                 "cinema")))
                                           (:cinema screening)))
        matches-language? (cond (= lang "non-dubbed")
                                  (fn [movie screening]
                                    (or (:original? screening)
                                        (= "de" (:original-lang movie))))
                                (= lang "originals-only")
                                  (fn [_movie screening] (:original? screening))
                                :else (constantly true))
        show-screening? (fn [movie screening]
                          (and (in-selected-location? movie screening)
                               (matches-language? movie screening)))]
    (filter #(-> %
                 :screenings
                 not-empty)
      (map (fn [movie]
             (update movie
                     :screenings
                     #(filter (fn [scr] (show-screening? movie scr)) %)))
        movies))))

;; TODO: validate query params
(defn upcoming-movies-handler
  [{:keys [db query-params]}]
  (let [form-state (if (empty? query-params) default-form-state query-params)
        base-url (-> config
                     :server
                     :base-url)
        upcoming-movies (filter-movies (get-movies-with-upcoming-screenings db)
                                       form-state)]
    (ok (render-upcoming-movies upcoming-movies base-url form-state)
        {"content-type" "text/html"})))

