(ns ov_movies.movie
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]))

(defn rand-id [] (str/join (map (fn [_] (rand-int 10)) (range 6))))

(s/def ::id (s/with-gen (s/and string? #(= 6 (count %)))
                        #(gen/fmap (fn [_] (rand-id)) (s/gen any?))))

(s/def ::title (s/and string? #(<= 3 (count %))))

(s/def ::poster (s/with-gen string?
                            #(gen/fmap (fn [uri] (str uri "/poster.jpg")) (s/gen uri?))))

(s/def ::movie (s/keys :req [::id ::title]
                       :opt [::poster]))