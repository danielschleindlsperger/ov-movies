(ns ov-movies.notification-test
  (:require [clojure.test :refer :all]
            [clojure.spec.gen.alpha :as gen]
            [ov-movies.crawl.notification :refer [notify!]]
            [clojure.spec.alpha :as s]))

(deftest send-notification
  (testing "does not send notification when no movies are passed"
    (is (= nil (notify! [] identity))))
  (testing "sends notification for one or more movies"
    (is (some? (notify! ["hi"] identity)))))