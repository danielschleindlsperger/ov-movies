(ns ov-movies.web-server
  (:require [org.httpkit.server :refer [run-server]]
            [ov-movies.handlers.create-handler :refer [create-handler]]
            [ov-movies.config :refer [config]]
            [taoensso.timbre :as log]))

(defonce server (atom nil))

(defn start-server []
  (let [port (-> config :server :port)]
    (reset! server (run-server (create-handler config) {:port port}))
    (log/debug (format "Listening @ http://localhost:%s" port))))

(defn stop-server []
  (when-not (nil? @server)
    (@server)
    (reset! server nil)))

(defn restart-server []
  (stop-server)
  (start-server))