(ns ov-movies.util
  (:require [clojure.string :as str]
            [ov-movies.config :refer [config]])
  (:import [java.time OffsetDateTime Instant ZonedDateTime]
           [java.time.format DateTimeFormatter]
           [java.time.temporal ChronoUnit]))

(defn parse-date
  "Parses a date in the format YYYY-MM-dd-HH-mm to a java `OffsetDateTime`"
  [s]
  (let [format (.withZone (DateTimeFormatter/ofPattern "uuuu-MM-dd-HH-mm") (:timezone config))
        zoned-date (ZonedDateTime/parse s format)]
    (.toOffsetDateTime zoned-date)))

;; Formatting

(defn date-diff [n]
  (cond (<= n 0) "today"
        (<= n 1) "tomorrow"
        :else (format "in %s days" n)))

(def formatter (DateTimeFormatter/ofPattern "E dd.LL. HH:mm"))
(defn format-date
  "Format a java.time.Instant to a user readable string."
  [^Instant instant]
  (let [now (OffsetDateTime/now (:timezone config))
        then-offset (-> (:timezone config) .getRules (.getOffset instant))
        then (.atOffset instant then-offset)
        in-days (.until (.toLocalDate now) (.toLocalDate then) ChronoUnit/DAYS)]
    (format "%s (%s)" (.format then formatter) (date-diff in-days))))

;; hickory html parsing

(defn hick-inner-text
  "Receives a hickory node and returns it's direct content as a string.
  Trims all content text nodes.
  Cannot return an empty string. Returns nil instead."
  [node]
  (let [text (->> node :content (filter string?) (map str/trim) (str/join ""))]
    (when-not (str/blank? text) text)))

;; misc

(defn find-first
  [f coll]
  (first (filter f coll)))
