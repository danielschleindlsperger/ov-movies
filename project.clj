(defproject ov-movies "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.postgresql/postgresql "42.2.2"]
                 [migratus "1.2.7"]
                 [hickory "0.7.1"]]
  :main ^:skip-aot ov-movies.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[migratus-lein "0.7.2"]]
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "com.mysql.jdbc.Driver"
                  :subprotocol "postgres"
                  :subname "//localhost/ov_movies"
                  :user "root"
                  :password "root"}})