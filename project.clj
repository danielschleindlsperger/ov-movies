(defproject ov-movies "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.cognitect.aws/api "0.8.408"]
                 [com.cognitect.aws/endpoints "1.1.11.705"]
                 [com.cognitect.aws/cloudformation "773.2.575.0"]
                 [hickory "0.7.1"]]
  :main ^:skip-aot ov-movies.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
;; lein trampoline run -m ov-movies.infrastructure