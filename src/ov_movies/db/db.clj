(ns ov_movies.db.db
  (:require [cognitect.aws.client.api :as aws]))

(def secretsmanager (aws/client {:api :secretsmanager}))

(aws/validate-requests secretsmanager true)

(:GetSecretValue (aws/ops secretsmanager))

(def local-connection {:subprotocol "postgres"
                       :subname     "//localhost/ov_movies"
                       :user        "root"
                       :password    "root"})

(def db-conn
  "The database connection. Either a connection string or a connection map."
  (let [secret-id (System/getenv "DATABASE_URL_SECRET_ID")]
    (if (some? secret-id)
      (:SecretString (aws/invoke secretsmanager {:op :GetSecretValue :request {:SecretId secret-id}}))
      local-connection)))
