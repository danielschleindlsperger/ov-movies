(defproject ov-movies "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/test.check "0.9.0"]           ;; This should actually be a "dev" dependency
                 [org.clojure/data.json "0.2.7"]
                 [org.postgresql/postgresql "42.2.2"]
                 [com.layerware/hugsql "0.5.1"]
                 [migratus "1.2.7"]
                 [hickory "0.7.1"]
                 [uswitch/lambada "0.1.2"]
                 [com.cognitect.aws/api "0.8.423"]
                 [com.cognitect.aws/endpoints "1.1.11.710"]
                 [com.cognitect.aws/secretsmanager "770.2.568.0"]
                 [clj-http "3.10.0"]]
  :target-path "target/%s"
  :profiles {:crawler {:main          ov_movies.crawler.crawler
                       :uberjar-name  "crawler.jar"
                       :aot           :all
                       :clean-targets [:compile-path]}
             :api     {:main          ov_movies.api.handler
                       :uberjar-name  "api.jar"
                       :aot           :all
                       :clean-targets [:compile-path]}}
  ;; Don't clean target folder since we build multiple jars in series
  :plugins [[migratus-lein "0.7.2"]]
  :migratus {:store         :database
             :migration-dir "migrations"
             :db            (or (System/getenv "DATABASE_URL") "jdbc:postgres://root:root@localhost:5432/ov_movies")})
