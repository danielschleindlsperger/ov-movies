(ns ov_movies.config
  (:import [java.time ZoneId]))

(def cfg {:timezone                    (ZoneId/of "Europe/Paris")
          :api-url                     (or (System/getenv "API_URL") "https://example.com")
          :pushover-user-key-secret-id (or (System/getenv "PUSHOVER_USER_KEY_SECRET_ID") "arn:aws:secretsmanager:eu-central-1:394586955256:secret:pushoveruserkey26A69E42-FdquWcdXvjfB-rJfcX4")
          :pushover-api-key-secret-id  (or (System/getenv "PUSHOVER_API_KEY_SECRET_ID") "arn:aws:secretsmanager:eu-central-1:394586955256:secret:pushoverapikeyD11A8EAC-SjkWq72aEwZl-jg3m0O")})