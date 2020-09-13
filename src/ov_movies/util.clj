(ns ov-movies.util
  (:require [clojure.string :as str]
            [ov-movies.config :refer [config]])
  (:import [java.time OffsetDateTime Instant ZonedDateTime]
           [java.time.format DateTimeFormatter]
           [java.time.temporal ChronoUnit]
           [java.sql Timestamp]))

(defn parse-date
  "Parses a date in the format YYYY-MM-dd-HH-mm to a java `OffsetDateTime`"
  [s]
  (let [format (.withZone (DateTimeFormatter/ofPattern "uuuu-MM-dd-HH-mm") (:timezone config))
        zoned-date (ZonedDateTime/parse s format)]
    (.toOffsetDateTime zoned-date)))

(defn offset-date-time->iso-offset-date-time-string
  "Stringify date to ISO-8601 compatible JSON datetime string"
  [^OffsetDateTime date]
  (.format date DateTimeFormatter/ISO_OFFSET_DATE_TIME))

(defn parse-zoned-date-time
  [s]
  (let [zoned-date (ZonedDateTime/parse s DateTimeFormatter/ISO_ZONED_DATE_TIME)]
    (.toOffsetDateTime zoned-date)))

(defn sqltimestamp->offsetdatetime
  "Converts a java.sql.Timestamp to a java.date.OffsetDateTime with timezone Europe/Berlin"
  [^Timestamp timestamp]
  (let [instant (Instant/ofEpochMilli (.getTime timestamp))]
    (OffsetDateTime/ofInstant instant (:timezone config))))

(defn date-diff [n]
  (cond (<= n 0) "today"
        (<= n 1) "tomorrow"
        :else (format "in %s days" n)))

(def formatter (DateTimeFormatter/ofPattern "E dd.LL. HH:mm"))
(defn format-date
  "Format a java.time.OffsetDateTime to a user readable string."
  [date]
  (let [now (OffsetDateTime/now (:timezone config))
        in-days (.until (.toLocalDate now) (.toLocalDate date) ChronoUnit/DAYS)]
    (format "%s (%s)" (.format date formatter) (date-diff in-days))))

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn hick-inner-text
  "Receives a hickory node and returns it's direct content as a string.
  Trims all content text nodes.
  Cannot return an empty string. Returns nil instead."
  [node]
  (let [text (->> node :content (filter string?) (map str/trim) (str/join ""))]
    (when-not (str/blank? text) text)))