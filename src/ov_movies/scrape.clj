(ns ov-movies.scrape
  (:require
    [clojure.string :as str]
    [hickory.core :as h]
    [hickory.select :as s]))

(def base-url "https://www.cineplex.de")
(def overview-url (str base-url "/programm/neufahrn/"))

(defn fetch-html [url] (h/as-hickory (h/parse (slurp url))))

(defn get-more-links [hickory-html]
  (s/select (s/class "schedule__grid-item--more") hickory-html))

(defn detail-urls
  "Crawls the overview page and returns relative urls to all movie detail pages in the page."
  []
  (let [html (fetch-html overview-url)
        links (get-more-links html)]
    (map #(-> % :attrs :href) links)))

(defn fetch-detail-page [rel-url] (fetch-html (str base-url rel-url)))

(defn title [detail-page]
  (-> (s/select (s/tag :h1) detail-page)
      first :content first str/trim))

(defn poster-image [detail-page]
  (-> (s/select (s/descendant (s/class "movie-poster") (s/tag :img)) detail-page)
      first :attrs :src))

(defn find-show [page-html]
  (s/select (s/child (s/class "performance-holder") (s/class "schedule__grid-item")) page-html))

(defn original?
  "Determines if the given show is to an original showing."
  [show]
  (-> (s/select (s/descendant (s/find-in-text #"Original")) show)
      count (not= 0)))

(defn showtime [show]
  "Takes a showtime schedule link and parses the showtime."
  (let [time-el (first (s/select (s/tag :time) show))
        date (-> time-el :attrs :datetime)
        time (-> time-el :content first (str/replace #":" "-") )]
    (str date "-" time)))

(defn parse-film
  "Takes the HTML of a film page and returns a parsed film with :title :poster and a vector of :original-dates"
  [detail-html]
  {:title (title detail-html)
   :poster (poster-image detail-html)
   :original-dates  (map showtime (filter original? (find-show detail-html)))})

(defn has-originals?
  "Determine if a parsed film has original shows."
  [film]
  (< 0 (count (:original-dates film))))

;; for testing
;(def url "/film/bad-boys-for-life/267153/neufahrn/#vorstellungen")
;(def detail-html (fetch-detail-page url))
;(def shows (find-show detail-html))
;(def originals (filter original? shows))
;(count originals)
;(showtime (first originals))
;(first originals)
;(has-originals? (parse-film detail-html))
;(title detail-html)
;(poster-image detail-html)
;; endtesting

;;; TODO:
;; Returns all unique movies with the cineplex id extracted
;; Return found screenings with cineplex id extracted

(defn movies-with-original-screenings []
  (filter has-originals? (map (comp parse-film fetch-detail-page) (detail-urls))))