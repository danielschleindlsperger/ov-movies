(ns ov-movies.util-test
  (:require [clojure.test :refer [deftest testing is]]
            [ov-movies.util :refer [parse-date]]))

(deftest test-parse-date
  (testing "parses date string with central european timezone"
    (let [parsed (parse-date "2020-01-01-20-00")]
      (is (= 2020 (.getYear parsed)))
      (is (= 1 (.getMonthValue parsed)))
      (is (= 1 (.getDayOfMonth parsed)))
      (is (= 20 (.getHour parsed))))))
