(ns ov_movies.api.handler
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [ov_movies.db.movies :as movies]
            [ov_movies.db.db :refer [db-conn]]))

(def not-found {:statusCode 404 :body "Not found." :headers {}})

(defn parse-movie-id [s] (last (re-find #"/blacklist/(.*)$" s)))
(defn- handle [req]
  (if-let [id (parse-movie-id (:path req))]
    (if-let [movie (movies/get-movie db-conn {:id id})]
      (do (movies/blacklist-movie db-conn {:id id})
          {:statusCode 200 :body (format "Successfuly blacklisted movie \"%s\"!" (:title movie)) :headers {}})
      not-found)
    not-found))

(deflambdafn ov_movies.api.handler
             [in out ctx]
             (let [body (-> in io/reader (json/read :key-fn keyword))
                   response {:statusCode 200 :body "hello world!" :headers {}}]
               (println body)
               (with-open [w (io/writer out)]
                 (json/write response w))))
