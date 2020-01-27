(ns ov_movies.scrape
  (:require
    [clojure.string :as str]
    [hickory.core :as h]
    [hickory.select :as s]
    [ov_movies.util :as u]))

(def base-url "https://www.cineplex.de")
(def overview-url (str base-url "/programm/neufahrn/"))

;;;
;;; Overview page: Parse the overview page and returning links to the detail pages.

(defn html->hickory
  "Converts an HTML string to a hickory data structure."
  [s]
  (-> s h/parse h/as-hickory))

(defn get-more-links [hickory-html]
  (s/select (s/class "schedule__grid-item--more") hickory-html))

(defn detail-urls
  "Parses the overview pages HTML and returns relative urls to all movie detail pages in the page."
  [html]
  (let [links (-> html html->hickory get-more-links)]
    (map #(-> % :attrs :href) links)))

;;;
;;; Detail page: Load and parse a detail page and retrieve all screening of the movie.

(defn fetch-detail-page [rel-url] (slurp (str base-url rel-url)))

(defn parse-movie-id
  "Takes a URL to a movie detail page and extracts the cineplex id from it."
  [url]
  (when url (last (re-find #"film/.*/(\d+)/" url))))

(defn parse-screening-id
  "Takes a URL to a booking page of a screening and extracts the cineplex id from it."
  [url]
  (when url (last (re-find #"performance/(.*)/mode/sale" url))))

(defn title
  "Parses a movies title from a detail pages hickory HTML."
  [detail-page]
  (let [s (-> (s/select (s/tag :h1) detail-page)
               first :content first)]
    (when (some? s) (str/trim s))))

(defn id-from-canonical
  "Takes HTML and returns the movie id parsed from the canonical url."
  [hick]
  (-> (s/select (s/attr "rel" #(= % "canonical")) hick) first :attrs :href parse-movie-id))

(defn poster-image
  "Parse a movies poster image url from a detail pages hickory HTML."
  [detail-page]
  (-> (s/select (s/descendant (s/class "movie-poster") (s/tag :img)) detail-page)
      first :attrs :src))

(defn find-show [page-html]
  (s/select (s/child (s/class "performance-holder") (s/class "schedule__grid-item")) page-html))

(defn original?
  "Determines if the given show is to an original showing."
  [show]
  (-> (s/select (s/descendant (s/find-in-text #"Original")) show)
      count (not= 0)))

(defn screening [show]
  "Takes a showtime schedule link and parses the :date and :id."
  (let [time-el (first (s/select (s/tag :time) show))
        date (-> time-el :attrs :datetime)
        time (-> time-el :content first (str/replace #":" "-"))
        url (-> show :attrs :href)]
    {:date (str date "-" time)
     :id (parse-screening-id url)}))

(defn parse-movie
  "Takes the HTML of a movie page and returns a parsed movie with :title :poster and a vector of :original-dates"
  [html]
  (let [hick-html (html->hickory html)]
    {:id (id-from-canonical hick-html)
     :title          (title hick-html)
     :poster         (poster-image hick-html)
     :original-dates (map screening (filter original? (find-show hick-html)))}))

(defn has-originals?
  "Determine if a parsed movie has original shows."
  [movie]
  (< 0 (count (:original-dates movie))))

;; for testing
;(def url "/film/bad-boys-for-life/267153/neufahrn/#vorstellungen")
;(def detail-html (fetch-detail-page url))
;(def shows (find-show detail-html))
;(def originals (filter original? shows))
;(count originals)
;(showtime (first originals))
;(first originals)
;(has-originals? (parse-movie detail-html))
;(title detail-html)
;(poster-image detail-html)
;; endtesting

(defn normalize-movie [movie]
  (let [dates (:original-dates movie)]
    (map #(-> %
              (assoc :movie_id (:id movie))
              (update :date u/parse-date)) dates)))

(defn normalize-scraped
  "Takes a list of parsed movies and returns a map of
  :movies (with their dates parsed) and
  :screenings (with :movie_id)"
  [xs]
  {:movies (map #(dissoc % :original-dates) xs)
   :screenings (flatten (map normalize-movie xs))})

(defn movies-with-original-screenings []
  (let [html (slurp overview-url)]
    (filter has-originals? (map (comp parse-movie fetch-detail-page) (detail-urls html)))))

(defn scrape! [] (normalize-scraped (movies-with-original-screenings)))