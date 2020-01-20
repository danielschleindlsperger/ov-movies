(ns ov_movies.util
  (:import [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(def central-europe (ZoneId/of"Europe/Paris"))

(defn parse-date
  "Parses a date in the format YYYY-MM-dd-HH-mm to a java `OffsetDateTime`"
  [s]
  (let [format (.withZone (DateTimeFormatter/ofPattern "uuuu-MM-dd-HH-mm") central-europe)
        zoned-date (ZonedDateTime/parse s format)]
    (.toOffsetDateTime zoned-date)))