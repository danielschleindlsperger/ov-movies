(ns ov-movies.util-test
  (:require [clojure.test :refer :all]
            [ov-movies.util :refer :all])
  (:import [java.time ZoneId LocalDateTime]))

(deftest test-parse-date
  (testing "parses date string with central european timezone"
    (let [parsed (parse-date "2020-01-01-20-00")
          paris-offset (-> (LocalDateTime/now)
                           (.atZone (ZoneId/of "Europe/Paris"))
                           .getOffset)]
      (is (= 2020 (.getYear parsed)))
      (is (= 1 (.getMonthValue parsed)))
      (is (= 1 (.getDayOfMonth parsed)))
      (is (= 20 (.getHour parsed)))
      (is (= paris-offset (.getOffset parsed))))))
