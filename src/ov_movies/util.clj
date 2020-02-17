(ns ov-movies.util
  (:import [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter])
  (:require [cognitect.aws.client.api :as aws]
            [ov-movies.config :refer [cfg]]))

(defn parse-date
  "Parses a date in the format YYYY-MM-dd-HH-mm to a java `OffsetDateTime`"
  [s]
  (let [format (.withZone (DateTimeFormatter/ofPattern "uuuu-MM-dd-HH-mm") (:timezone cfg))
        zoned-date (ZonedDateTime/parse s format)]
    (.toOffsetDateTime zoned-date)))

(def secretsmanager (aws/client {:api :secretsmanager}))
(aws/validate-requests secretsmanager true)
(defn fetch-sm-secret
  [secret-id]
  "Takes an AWS Secrets Manager Secret ARN and returns its SecretString or nil if it isn't defined."
  (:SecretString (aws/invoke secretsmanager {:op :GetSecretValue :request {:SecretId secret-id}})))

(defn find-first
  [f coll]
  (first (filter f coll)))