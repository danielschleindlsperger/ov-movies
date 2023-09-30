(ns ov-movies.movie-api
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]))

(def ^:private base-url "https://api.themoviedb.org/3")

(defn search-movie
  "Search for a movie by its title. Returns a single movie or `nil`."
  [api-key query]
  {:pre [some? query]}
  (let [url (str base-url "/search/movie")
        query-params {"api_key" api-key, "query" query}
        result (client/get url
                           {:headers {"Content-Type" "application/json"},
                            :query-params query-params})
        success? (= 200 (:status result))]
    (if success?
      (-> (json/read-str (:body result) :key-fn ->kebab-case-keyword)
          :results
          first)
      (throw (ex-info "Searching for movie failed."
                      {:query query, :result result})))))

(comment
  (def api-key
    (get-in (var-get (requiring-resolve 'ov-movies.config/config))
            [:movie-db :api-key]))
  (search-movie api-key "pumuckl")
  (search-movie api-key "Nomadland"))