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
           (csv-pars/read-csv-lines (io/file "test-resources/csv-data/csvb.csv"))))))

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
           (csv-pars/load-csv "test-resources/csv-data")))))

(deftest to-named-columns
  (testing "should build up named columns from parsed csv-data"
    (let [csv-data (csv-pars/load-csv "test-resources/csv-data")]
      (is (= [{:a "foo"
               :b "bar"
               :c "baz"}
              {:a "fooo"
               :b "barr"
               :c "bazz"}]
             (csv-pars/with-columns csv-data
                                    [:a :b :c]))))))

(deftest to-special-columns
  (testing "should build up columns from column-spec"
    (let [csv-data (csv-pars/load-csv "test-resources/csv-special-data")]
      (is (= [{:a (t/date-time 2016 5 18)
               :b -16.13
               :d "baz"}
              {:a (t/date-time 2016 5 12)
               :b 100000.1122
               :d "baf"}]
             (csv-pars/with-columns csv-data
                                    [[:a {:type   :date
                                          :format "dd.MM.yyyy"}]
                                     [:b {:type   :number
                                          :locale Locale/GERMAN}]
                                     nil
                                     :d]))))))

(deftest read-data-spec
  (testing "should read the data-spec"
    (let [data-spec (csv-pars/load-data-spec "test-resources/data-spec/data-spec.edn")]
      (is (= [[:a {:type   :date
                   :format "dd.MM.yyyy"}]
              [:b {:type   :number
                   :locale java.util.Locale/GERMAN}]
              nil
              :d]
             data-spec))
      (is (= [{:a (t/date-time 2016 5 18)
               :b -16.13
               :d "baz"}
              {:a (t/date-time 2016 5 12)
               :b 100000.1122
               :d "baf"}]
             (csv-pars/with-columns (csv-pars/load-csv "test-resources/csv-special-data") data-spec))))))
