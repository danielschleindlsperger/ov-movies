(ns ov_movies.screening
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str])
  (:import [java.time OffsetDateTime ZoneOffset ZonedDateTime]))


(s/def ::id (s/with-gen (s/and string? #(-> % count (> 2)))
                        #(gen/string-alphanumeric)))

(s/def ::date (s/with-gen any?
                          ;; TODO: generate, don't use "now()"
                          #(gen/fmap (fn [_] (.toOffsetDateTime (ZonedDateTime/now))) (s/gen any?))))

(s/def ::movie_id (s/and string? #(-> % count (> 2))))

(s/def ::screening (s/keys :req [::id ::date]
                           :opt [::movie_id]))

(gen/sample (s/gen ::screening) 2)