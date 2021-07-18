(ns ov-movies.movie
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [next.jdbc :as jdbc]
            [honeysql.types :refer [raw]]
            [honeysql.helpers]
            [honeysql-postgres.format]
            [honeysql.format :refer [format] :rename {format sql-format}]
            [ov-movies.database :refer [db-opts]]))

(defn rand-id [] (str/join (map (fn [_] (rand-int 10)) (range 6))))

(s/def ::id (s/with-gen (s/and string? #(= 6 (count %)))
                        #(gen/fmap (fn [_] (rand-id)) (s/gen any?))))

(s/def ::title (s/and string? #(<= 3 (count %))))

(s/def ::poster (s/with-gen string?
                            #(gen/fmap (fn [uri] (str uri "/poster.jpg")) (s/gen uri?))))

(s/def ::movie (s/keys :req [::id ::title]
                       :opt [::poster]))

(defn insert-movies-query [movies]
  (sql-format {:insert-into   :movies
               :values        movies
               :on-conflict   [:id]
               :do-update-set [:title :description :poster :original-lang]
               :returning     [:*]}
              :quoting :ansi))

(defn blacklist-movie-query [id]
  (sql-format {:update    :movies
               :set       {:blacklisted true}
               :where     [:= :id id]
               :returning [:*]}
              :quoting :ansi))

(sql-format {:select [:*]
             :from   [:screenings]
             :where  [:and [:in :movie_id (map :movie-id [{:movie-id "foo"}])] [:= :blacklisted false]]})

(defn get-movies-with-upcoming-screenings [db]
  (jdbc/with-transaction [tx db]
                         (let [upcoming-screenings (jdbc/execute! tx (sql-format {:select   [:*]
                                                                                  :from     [:screenings]
                                                                                  :order-by [[:date :asc]]
                                                                                  :where    [:> :date (raw "now()")]}) db-opts)
                               upcoming-movies (if (empty? upcoming-screenings)
                                                 []
                                                 (jdbc/execute! tx (sql-format {:select   [:*]
                                                                                :from     [:movies]
                                                                                :where    [:and [:in :id (distinct (map :movie-id upcoming-screenings))] [:= :blacklisted false]]
                                                                                :order-by [[:title :asc]]}) db-opts))
                               screenings-by-movie-id (group-by :movie-id upcoming-screenings)]
                           (map (fn [movie] (assoc movie :screenings (get screenings-by-movie-id (:id movie) []))) upcoming-movies))))

(defn insert-movies! [db movies]
  (let [stmt (insert-movies-query movies)]
    (jdbc/execute! db stmt db-opts)))

(defn blacklist-movie! [db {id :id}] (jdbc/execute! db (blacklist-movie-query id) db-opts))