(defproject ov-movies "0.1.0"
  :min-lein-version "2.9.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/test.check "0.9.0"]           ;; This should actually be a "dev" dependency
                 [org.clojure/data.json "0.2.7"]

                 ;; http
                 [http-kit "2.3.0"]
                 [metosin/reitit-ring "0.4.2"]
                 [ring/ring-core "1.8.0"]

                 ;; logging
                 [com.taoensso/timbre "4.10.0"]

                 ;; config
                 [aero "1.1.6"]

                 ;; DB stuff
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.2"]
                 [honeysql "0.9.8"]
                 [nilenso/honeysql-postgres "0.2.6"]
                 [migratus "1.2.7"]

                 ;; html
                 [hiccup "1.0.5"]
                 [hickory "0.7.1"]]
  :main ^:skip-aot ov-movies.main
  :resource-paths ["resources" "target/resources"]
  :target-path "target/%s"
  :profiles {:dev          [:project/dev :profiles/dev]
             :repl         {:prep-tasks   ^:replace ["javac" "compile"]
                            :repl-options {:init-ns user}}
             :uberjar      {:aot :all}
             :profiles/dev {}
             :project/dev  {:source-paths   ["dev/src"]
                            :resource-paths ["dev/resources"]
                            :dependencies   [[eftest "0.5.7"]]}}
  :plugins [[migratus-lein "0.7.2"]
            [lein-kibit "0.1.8"]]
  :middleware []
  :migratus {:store         :database
             :migration-dir "migrations"
             :db            (or (System/getenv "DATABASE_URL") "jdbc:postgres://root:root@localhost:5432/ov_movies")})
