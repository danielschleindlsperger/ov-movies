(ns ov-movies.scrape-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :refer [resource]]
            [ov-movies.crawl.scrape :refer :all]))

(deftest test-detail-urls
  (let [html (slurp (resource "test/cineplex-overview.html"))
        expected ["/film/die-hochzeit/350336/neufahrn/#vorstellungen"
                  "/film/1917/362504/neufahrn/#vorstellungen"
                  "/film/jojo-rabbit/365150/neufahrn/#vorstellungen"]]
    (testing "parses urls"
      (is (= expected (detail-urls html))))
    (testing "doesn't explode with bad input"
      (is (= '[] (detail-urls ""))))))

(deftest test-parse-movie
  (let [html (slurp (resource "test/bad-boys-for-life.html"))
        expected {:id             "267153"
                  :title          "Bad Boys for Life"
                  :description    "Die Bad Boys Mike Lowrey (Will Smith) und Marcus Burnett (Martin\n        Lawrence) sind gemeinsam zurück für einen letzten großen Ritt in der mit Spannung erwarteten Actionkomödie BAD\n        BOYS FOR LIFE. (Quelle: Verleih)"
                  :poster         "https://cdn.cineplex.de/_imageserver/340f267153.jpg"
                  :original-dates [{:date "2020-01-22-21-55" :id "E1DE9000023AIYWYCE"}
                                   {:date "2020-01-23-22-10" :id "EDFE9000023AIYWYCE"}
                                   {:date "2020-01-24-22-10" :id "941F9000023AIYWYCE"}
                                   {:date "2020-01-25-19-25" :id "8D0F9000023AIYWYCE"}
                                   {:date "2020-01-26-21-50" :id "B41F9000023AIYWYCE"}
                                   {:date "2020-01-27-19-25" :id "AD0F9000023AIYWYCE"}
                                   {:date "2020-01-28-22-10" :id "D41F9000023AIYWYCE"}
                                   {:date "2020-01-29-19-25" :id "CD0F9000023AIYWYCE"}]}]
    (testing "parses the movie from html"
      (is (= expected (parse-movie html))))
    (testing "does not explode with bad input"
      (is (= {:id             nil
              :title          nil
              :description    nil
              :poster         nil
              :original-dates []} (parse-movie ""))))))

(deftest test-title
  (let [dolittle "
    <div><h1 class=\"film-title\">
      <span class=\"film-film-attributes\"><i class=\"icon-junior-label\"></i></span>
        Die fantastische Reise des Dr. Dolittle
      <span class=\"film-film-attributes\">
      <i class=\"icon-neu\"></i><i class=\"icon-greta_starks-label\"></i>
      </span>
     </h1></div>"
        bad-boys "<div><h1 class=\"film-title\">Bad Boys for Life</h1></div>"]
    (testing "parses simple h1 with text content"
      (is (= "Bad Boys for Life" (title (html->hickory bad-boys)))))
    (testing "parses h1 with tags and icons"
      (is (= "Die fantastische Reise des Dr. Dolittle" (title (html->hickory dolittle)))))))

(deftest test-parse-movie-id
  (testing "parses movie id from url"
    (is (= "339173" (parse-movie-id "film/spione-undercover/339173/neufahrn/#vorstellungen"))))
  (testing "doesn't explode if it doesn't find anything"
    (is (= nil (parse-movie-id ""))))
  (testing "doesn't explode on nil"
    (is (= nil (parse-movie-id nil)))))

(deftest test-parse-screening-id
  (testing "parse screening id from url"
    (is (= "EDFE9000023AIYWYCE" (parse-screening-id "https://booking.cineplex.de/#/site/106/performance/EDFE9000023AIYWYCE/mode/sale/"))))
  (testing "doesn't explode if it doesn't find anything"
    (is (= nil (parse-screening-id ""))))
  (testing "doesn't explode on nil"
    (is (= nil (parse-screening-id nil)))))

(deftest test-has-originals?
  (let [film {:title          "title"
              :poster         "https://poster.de/image.jpg"
              :original-dates ["2020-01-01-20-00"]}]
    (testing "has originals"
      (is (= true (has-originals? film))))

    (testing "has no originals"
      (is (= false (has-originals? (assoc film :original-dates [])))))

    (testing "nil"
      (is (= false (has-originals? nil))))))

(deftest test-normalize-scraped
  (let [parsed [{:id             "267153"
                 :title          "Bad Boys for Life"
                 :poster         "poster.url"
                 :original-dates [{:date "2020-01-22-21-55" :id "E1DE9000023AIYWYCE"}]}]]
    (testing "normalizes"
      (is (= {:movies     [{:id     "267153"
                            :title  "Bad Boys for Life"
                            :poster "poster.url"}]
              :screenings [{:id       "E1DE9000023AIYWYCE"
                            :date     (ov-movies.util/parse-date "2020-01-22-21-55")
                            :movie_id "267153"}]} (normalize-scraped parsed))))
    (testing "does not explode on empty input"
      (is (= {:movies     []
              :screenings []} (normalize-scraped []))))))