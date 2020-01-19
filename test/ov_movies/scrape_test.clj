(ns ov-movies.scrape-test
  (:require [clojure.test :refer :all]
            [ov-movies.scrape :as scrape]))

(def film {:title          "title"
           :poster         "https://poster.de/image.jpg"
           :original-dates ["2020-01-01-20-00"]})

(deftest test-has-originals?
  (testing "has originals"
    (is (= true (scrape/has-originals? film))))

  (testing "has no originals"
    (is (= false (scrape/has-originals? (assoc film :original-dates [])))))

  (testing "nil"
    (is (= false (scrape/has-originals? nil)))))
