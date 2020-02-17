(ns ov-movies.screening
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import [java.time OffsetDateTime ZonedDateTime]))


(s/def ::id (s/with-gen (s/and string? #(-> % count (> 2)))
                        #(gen/string-alphanumeric)))

(s/def ::date (s/with-gen (partial type OffsetDateTime)
                          ;; TODO: generate, don't use "now()"
                          #(gen/fmap (fn [_] (.toOffsetDateTime (ZonedDateTime/now))) (s/gen any?))))

(s/def ::movie_id (s/and string? #(-> % count (> 2))))

(s/def ::screening (s/keys :req [::id ::date]
                           :opt [::movie_id]))