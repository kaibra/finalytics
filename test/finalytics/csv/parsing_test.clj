(ns finalytics.csv.parsing-test
  (:require [clojure.test :refer :all]
            [finalytics.csv.parsing :as csv-pars]
            [clj-time.core :as t]
            [clojure.java.io :as io])
  (:import (java.util Locale)))

(deftest reading-csv-file
  (testing "should read a single csv-file"
    (is (= ["\"foo  \"  ;\"bar  \";\"   baz\""
            "\"fooo  \"; \" barr\";\"bazz\""]
           (csv-pars/read-csv-lines (io/file "test-resources/data/csv-a/csv2.csv"))))))

(deftest parsing-csv-data-set
  (testing "should parse a single csv-data line"
    (is (= ["foo" "bar" "baz"]
           (csv-pars/parse-single-csv-line ";" "\"foo    \"   ;\"bar\";\"baz\""))))

  (testing "should parse csv-data-set"
    (is (= [["foo" "bar" "baz"]
            ["fooo" "barr" "bazz"]]
           (csv-pars/parse-csv-data #{"\"foo    \"    ;\"bar\";\"baz\""
                                      "\"fooo\";\"barr\";\"bazz\""} ";")))))

(deftest loading-csv-files-from-folder
  (testing "should load and merge multiple csv-files"
    (is (= [["foo" "bar" "baz"]
            ["fooo" "barr" "bazz"]]
           (csv-pars/load-csv "test-resources/data/csv-a")))))

(deftest to-named-columns
  (testing "should build up named columns from parsed csv-data"
    (let [csv-data (csv-pars/load-csv "test-resources/data/csv-a")]
      (is (= [{:columns {:a "foo"
                         :b "bar"
                         :c "baz"}}
              {:columns {:a "fooo"
                         :b "barr"
                         :c "bazz"}}]
             (csv-pars/with-columns csv-data
                                    [:a :b :c]))))))

(deftest to-special-columns
  (testing "should build up columns from column-spec"
    (let [csv-data (csv-pars/load-csv "test-resources/data/csv-b")]
      (is (= [{:columns {:a {:day   11
                             :month 5
                             :year  2016}
                         :b 1000.0
                         :d "This is a clientb transaction"}}
              {:columns {:a {:day   18
                             :month 5
                             :year  2016}
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:columns {:a {:day   12
                             :month 5
                             :year  2016}
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a {:day   10
                             :month 5
                             :year  2016}
                         :b -100.11
                         :d "unknown-stuff"}}
              {:columns {:a {:day   11
                             :month 5
                             :year  2016}
                         :b -1000.0
                         :d "This is a clientb transaction one more time"}}]
             (csv-pars/with-columns csv-data
                                    [[:a {:type   :date
                                          :format "dd.MM.yyyy"}]
                                     [:b {:type   :number
                                          :locale Locale/GERMAN}]
                                     nil
                                     :d]))))))

(deftest with-tids
  (testing "should add tids to rows"
    (is (= [{:columns {:a "foo"
                       :b "bar"
                       :c "baz"}
             :tid     :mister-foo}
            {:columns {:a "fOoo"
                       :b "barr"
                       :c "bazz"}}]
           (csv-pars/with-tids [{:columns {:a "foo"
                                           :b "bar"
                                           :c "baz"}}
                                {:columns {:a "fOoo"
                                           :b "barr"
                                           :c "bazz"}}]
                               {#".*foo.*" :mister-foo}
                               :a)))))

(deftest with-classifications
  (testing "should add classifications to rows"
    (is (= [{:classifications [:asian-food]
             :columns         {:a "foo"
                               :b "bar"
                               :c "baz"}
             :tid             :mister-foo}
            {:columns {:a "fOoo"
                       :b "barr"
                       :c "bazz"}}]
           (csv-pars/with-classification [{:columns {:a "foo"
                                                     :b "bar"
                                                     :c "baz"}
                                           :tid     :mister-foo}
                                          {:columns {:a "fOoo"
                                                     :b "barr"
                                                     :c "bazz"}}]
                                         {:asian-food [:mister-foo]})))))

(deftest sorting-rows
  (testing "should sort rows by date-sort-column"
    (is (= [{:columns {:a {:day   1
                           :month 5
                           :year  2015}}}
            {:columns {:a {:day   10
                           :month 5
                           :year  2016}}}
            {:columns {:a {:day   15
                           :month 5
                           :year  2016}}}
            {:columns {:a {:day   11
                           :month 10
                           :year  2016}}}
            {:columns {:a {:day   1
                           :month 1
                           :year  2017}}}]
           (csv-pars/sorted-rows [{:columns {:a {:day   15
                                                 :month 5
                                                 :year  2016}}}
                                  {:columns {:a {:day   11
                                                 :month 10
                                                 :year  2016}}}
                                  {:columns {:a {:day   1
                                                 :month 5
                                                 :year  2015}}}
                                  {:columns {:a {:day   1
                                                 :month 1
                                                 :year  2017}}}
                                  {:columns {:a {:day   10
                                                 :month 5
                                                 :year  2016}}}]
                                 :a))))

  (testing "should sort other columns with default behaviour"
    (is (= [{:columns {:a "A"}}
            {:columns {:a "B"}}
            {:columns {:a "C"}}
            {:columns {:a "D"}}
            {:columns {:a "E"}}]
           (csv-pars/sorted-rows [{:columns {:a "B"}}
                                  {:columns {:a "D"}}
                                  {:columns {:a "A"}}
                                  {:columns {:a "C"}}
                                  {:columns {:a "E"}}]
                                 :a)))
    ))

(deftest loading-complete-parsed-csv-data
  (testing "should load the whole test-data set"
    (is (= {2016 {5 {10 [{:columns {:a      {:day   10
                                             :month 5
                                             :year  2016}
                                    :b      -100.11
                                    :client "unknown-stuff"}}]
                     11 [{:classifications [:food]
                          :columns         {:a      {:day   11
                                                     :month 5
                                                     :year  2016}
                                            :b      1000.0
                                            :client "This is a clientb transaction"}
                          :tid             :clientb}
                         {:classifications [:food]
                          :columns         {:a      {:day   11
                                                     :month 5
                                                     :year  2016}
                                            :b      -1000.0
                                            :client "This is a clientb transaction one more time"}
                          :tid             :clientb}]
                     12 [{:classifications [:food]
                          :columns         {:a      {:day   12
                                                     :month 5
                                                     :year  2016}
                                            :b      100000.1122
                                            :client "This is a clientb transaction"}
                          :tid             :clientb}]
                     18 [{:classifications [:gas :food]
                          :columns         {:a      {:day   18
                                                     :month 5
                                                     :year  2016}
                                            :b      -16.13
                                            :client "Thank you says clienta"}
                          :tid             :clienta}]}}}
           (csv-pars/load-parsed-csv-data "test-resources/data/spec.edn" "test-resources/data/csv-b")))))

(deftest grouping-by-time
  (testing "should group entries by their time"
    (is (= {2015 {5 {1 [{:columns {:date {:day   1
                                          :month 5
                                          :year  2015}}}]}
                  6 {1 [{:columns {:date {:day   1
                                          :month 6
                                          :year  2015}}}]}}
            2016 {2 {2 [{:columns {:date {:day   2
                                          :month 2
                                          :year  2016}}}
                        {:columns {:date {:day   2
                                          :month 2
                                          :year  2016}}}]}
                  3 {2 [{:columns {:date {:day   2
                                          :month 3
                                          :year  2016}}}]}
                  7 {10 [{:columns {:date {:day   10
                                           :month 7
                                           :year  2016}}}]}}}
           (csv-pars/group-by-date-column
             [{:columns {:date {:day   1
                                :month 5
                                :year  2015}}}
              {:columns {:date {:day   1
                                :month 6
                                :year  2015}}}
              {:columns {:date {:day   2
                                :month 2
                                :year  2016}}}
              {:columns {:date {:day   2
                                :month 2
                                :year  2016}}}
              {:columns {:date {:day   2
                                :month 3
                                :year  2016}}}
              {:columns {:date {:day   10
                                :month 7
                                :year  2016}}}]
             :date)))))


(deftest grouping-lists
  (testing "should group lists at any depth "
    (let [input {123 [{:columns {:date {:day   1
                                        :month 5
                                        :year  2015}}}
                      {:columns {:date {:day   1
                                        :month 6
                                        :year  2015}}}]}]
      (is (= {123 {1 [{:columns {:date {:day   1
                                        :month 5
                                        :year  2015}}}
                      {:columns {:date {:day   1
                                        :month 6
                                        :year  2015}}}]}}
             (csv-pars/group-data-by input [:columns :date :day])))
      (is (= {123 {5 [{:columns {:date {:day   1
                                        :month 5
                                        :year  2015}}}]
                   6 [{:columns {:date {:day   1
                                        :month 6
                                        :year  2015}}}]}}
             (csv-pars/group-data-by input [:columns :date :month]))))))
