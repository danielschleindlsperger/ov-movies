(ns ov-movies.util
  (:import [java.time OffsetDateTime Instant ZonedDateTime]
           [java.time.format DateTimeFormatter]
           [java.sql Timestamp])
  (:require [ov-movies.config :refer [config]]))

(defn parse-date
  "Parses a date in the format YYYY-MM-dd-HH-mm to a java `OffsetDateTime`"
  [s]
  (let [format (.withZone (DateTimeFormatter/ofPattern "uuuu-MM-dd-HH-mm") (:timezone config))
        zoned-date (ZonedDateTime/parse s format)]
    (.toOffsetDateTime zoned-date)))

(defn parse-zoned-date-time
  [s]
  (let [zoned-date (ZonedDateTime/parse s DateTimeFormatter/ISO_ZONED_DATE_TIME)]
    (.toOffsetDateTime zoned-date)))

(defn sqltimestamp->offsetdatetime
  "Converts a java.sql.Timestamp to a java.date.OffsetDateTime with timezone Europe/Berlin"
  [^Timestamp timestamp]
  (let [instant (Instant/ofEpochMilli (.getTime timestamp))]
    (OffsetDateTime/ofInstant instant (:timezone config))))

(defn find-first
  [f coll]
  (first (filter f coll)))