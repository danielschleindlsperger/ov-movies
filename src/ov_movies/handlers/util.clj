(ns ov-movies.handlers.util)

;; HTTP helpers

(defn not-found
  ([] {:status 404 :headers {"content-type" "text/plain"} :body "not found."})
  ([body] {:stauts 404 :headers {"content-type" "text/plain"} :body body}))

(defn server-error
  ([] {:status 500 :headers {"content-type" "text/plain"} :body "internal server error."})
  ([body] {:status 500 :headers {"content-type" "text/plain"} :body body}))

(defn ok
  ([body] {:status 200 :headers {"content-type" "text/plain"} :body body})
  ([body headers] {:status 200 :headers (merge {"content-type" "text/plain"} headers) :body body}))

(defn temporary-redirect [location] {:status 307 :headers {"Location" location}})
