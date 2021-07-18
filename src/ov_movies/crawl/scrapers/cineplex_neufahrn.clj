(ns ov-movies.crawl.scrapers.cineplex-neufahrn
  (:require
    [clojure.string :as str]
    [hickory.core :refer [parse, as-hickory]]
    [hickory.select :as sel]
    [ov-movies.util :as u :refer [parse-date hick-inner-text]]))

(def base-url "https://www.cineplex.de")
(def overview-url (str base-url "/programm/neufahrn/"))

(defn- detail-urls
  "Parse the movie listing overview page for links (urls) to the movies detail pages and return them."
  [overview-html]
  (->> overview-html
       parse
       as-hickory
       (sel/select (sel/class "filmInfoLink"))
       (map #(-> % :attrs :href))))

;;;
;;; Detail page: Load and parse a detail page and retrieve all screening of the movie.

(defn- parse-movie-id
  "Takes a web page as a hickory data structure and finds the cineplex id."
  [hick]
  (let [meta-tag (sel/select (sel/child (sel/tag :head) (sel/and (sel/tag :meta) (sel/attr :name #(= % "cineplex:filmId")))) hick)
        content (-> meta-tag first :attrs :content)]
    (when content (str/trim content))))

(defn- parse-movie-title
  "Takes a web page asa hickory data structure and finds the movie's title."
  [hick]
  (let [h1 (first (sel/select (sel/tag :h1) hick))
        title (hick-inner-text h1)]
    title))

(defn original?
  "Determines if the given show is to an original showing."
  [show]
  (-> (sel/select (sel/descendant (sel/find-in-text #"Original")) show)
      count (not= 0)))

(defn- parse-screening
  "Takes a showtime schedule link and parses the :date and :id."
  [show]
  (let [time-el (first (sel/select (sel/tag :time) show))
        date (-> time-el :attrs :datetime)
        time (-> time-el :content first str/trim (str/replace #":" "-"))]
    {:date      (parse-date (str date "-" time))
     :original? (original? show)}))

(defn- parse-screenings
  "Takes a web page as a hickory data structure and parses the movie's screening dates.
   A screening has the following structure:
   :date
   :original"
  [hick]
  (let [screenings-elements (sel/select (sel/child (sel/class "performance-holder") (sel/class "schedule__grid-item")) hick)]
    (map parse-screening screenings-elements)))

(defn- parse-movie
  "Takes the HTML of a movie page and returns a parsed movie with :title :poster and a vector of :original-dates"
  [html]
  (let [hick-html (-> html parse as-hickory)]
    {:id         (parse-movie-id hick-html)
     :title      (parse-movie-title hick-html)
     :screenings (parse-screenings hick-html)}))

(defn- fetch-detail-page [rel-url] (slurp (str base-url rel-url)))

(defn scrape! []
  (let [overview-html (slurp overview-url)
        movies (pmap (comp parse-movie fetch-detail-page) (detail-urls overview-html))]
    movies))

(comment
  (def html (slurp overview-url))
  (detail-urls html)
  (time (scrape!))
  (filter #(some :original? (:screenings %)) (scrape!)))