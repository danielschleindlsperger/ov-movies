(defproject ov-movies "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/test.check "0.9.0"]           ;; This should actually be a "dev" dependency
                 [org.clojure/data.json "0.2.7"]
                 [duct/core "0.8.0"]
                 [hawk "0.2.11"]
                 [duct/module.logging "0.4.0"]
                 [duct/module.web "0.7.0"]
                 [duct/server.http.http-kit "0.1.4"]

                 ;; DB stuff
                 [duct/module.sql "0.6.0"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.2"]
                 [com.layerware/hugsql "0.5.1"]
                 [migratus "1.2.7"]

                 ;; AWS stuff
                 [hickory "0.7.1"]
                 [com.cognitect.aws/api "0.8.423"]
                 [com.cognitect.aws/endpoints "1.1.11.710"]
                 [com.cognitect.aws/secretsmanager "770.2.568.0"]

                 [clj-http "3.10.0"]]
  :main ^:skip-aot ov-movies.main
  :resource-paths ["resources" "target/resources"]
  :target-path "target/%s"
  :prep-tasks ["javac" "compile" ["run" ":duct/compiler"]]
  :profiles {:crawler      {:main          ov-movies.crawl.crawler
                            :uberjar-name  "crawler.jar"
                            :aot           :all
                            ;; Don't clean target folder since we build multiple jars in series
                            :clean-targets [:compile-path]}
             :api          {:main          ov-movies.api.handler
                            :uberjar-name  "api.jar"
                            :aot           :all
                            :clean-targets [:compile-path]}

             :dev          [:project/dev :profiles/dev]
             :repl         {:prep-tasks   ^:replace ["javac" "compile"]
                            :repl-options {:init-ns user}}
             :uberjar      {:aot :all}
             :profiles/dev {}
             :project/dev  {:source-paths   ["dev/src"]
                            :resource-paths ["dev/resources"]
                            :dependencies   [[integrant/repl "0.3.1"]
                                             [eftest "0.5.7"]
                                             [kerodon "0.9.0"]]}}
  :plugins [[migratus-lein "0.7.2"] [duct/lein-duct "0.12.1"]]
  :middleware [lein-duct.plugin/middleware]
  :migratus {:store         :database
             :migration-dir "migrations"
             :db            (or (System/getenv "DATABASE_URL") "jdbc:postgres://root:root@localhost:5432/ov_movies")})
