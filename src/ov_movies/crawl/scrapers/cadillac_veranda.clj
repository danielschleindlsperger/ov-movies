(ns ov-movies.crawl.scrapers.cadillac-veranda
  (:require
    [clojure.string :as str]
    [clojure.set :as set]
    [hickory.core :refer [parse, as-hickory]]
    [hickory.select :as sel]
    [clojure.data.json :as json])
  (:import (java.time Instant)))

(def base-url "https://cadillac.movieplace.de")
(def overview-url (str base-url "/programm"))

(defn original?
  "Determines if the given screening is to an original showing.
   \"videoFormat\" is a pipe separated list in a string: \"2D|OmU|OV\"\n  "
  [screening]
  (let [original-formats #{"OV" "OmU"}
        parsed-formats (-> screening :videoFormat (str/split #"\|") set)]
    (not (= #{} (set/intersection original-formats parsed-formats)))))

(defn parse-movie-name [screening]
  (let [raw-title (:name screening)
        video-fmt (:videoFormat screening)]
    (when (not-empty raw-title)
      (str/trim (first (str/split raw-title (re-pattern video-fmt)))))))

(defn- combine-screenings [screenings]
  (reduce (fn [movie screening]
            (merge movie {:id         (or (:movie-id movie) (:movie-id screening))
                          :title      (or (:movie-title movie) (:movie-title screening))
                          :screenings (conj (:screenings movie) {:id        (:id screening)
                                                                 :date      (:date screening)
                                                                 :original? (:original? screening)})}))
          {:screenings []}
          screenings))

(defn- extract-movies-with-screenings [events]
  (->> events
       (map (fn [screening]
              {:movie-id    (get-in screening [:workPresented (keyword "@id")])
               :movie-title (parse-movie-name screening)
               :date        (Instant/parse (:startDate screening))
               :id          (java.util.UUID/randomUUID)
               :original?   (original? screening)}))
       (group-by :movie-id)
       (map (fn [[_ screenings-for-movie]] (combine-screenings screenings-for-movie)))))

(defn parse-movies [overview-html]
  (let [overview-htree (-> overview-html parse as-hickory)
        ld-json (first (sel/select (sel/and (sel/tag :script)
                                            (sel/attr :type #(= % "application/ld+json")))
                                   overview-htree))
        screening-events (-> ld-json :content first (json/read-str :key-fn keyword))]
    (extract-movies-with-screenings screening-events)))

(defn scrape! []
  (parse-movies (slurp overview-url)))

(comment
  (def html (slurp overview-url))
  (parse-movies html)
  (time (scrape!))
  (filter #(some :original? (:screenings %)) (scrape!)))