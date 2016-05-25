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
    (let [csv-data (csv-pars/load-csv "test-resources/csv-special-data")]
      (is (= [{:columns {:a (t/date-time 2016 5 18)
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:columns {:a (t/date-time 2016 5 12)
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a (t/date-time 2016 5 10)
                         :b -100.11
                         :d "unknown-stuff"}}]
             (csv-pars/with-columns csv-data
                                    [[:a {:type   :date
                                          :format "dd.MM.yyyy"}]
                                     [:b {:type   :number
                                          :locale Locale/GERMAN}]
                                     nil
                                     :d]))))))

(deftest read-data-spec
  (let [data-spec (csv-pars/load-data-spec "test-resources/data-spec/data-spec.edn")
        tid-spec (csv-pars/load-tid-spec "test-resources/data-spec/tid-spec.edn")
        with-cols (csv-pars/with-columns (csv-pars/load-csv "test-resources/csv-special-data") data-spec)]
    (testing "should read the data-spec"
      (is (= [[:a {:type   :date
                   :format "dd.MM.yyyy"}]
              [:b {:type   :number
                   :locale java.util.Locale/GERMAN}]
              nil
              :d]
             data-spec)))

    (testing "should build up columns"
      (is (= [{:columns {:a (t/date-time 2016 5 18)
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:columns {:a (t/date-time 2016 5 12)
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a (t/date-time 2016 5 10)
                         :b -100.11
                         :d "unknown-stuff"}}]
             (csv-pars/with-columns (csv-pars/load-csv "test-resources/csv-special-data") data-spec))))

    (testing "should load the tid file"
      (is (= {"^.*clienta.*" :clienta
              "^.*clientb.*" :clientb}
             (->> tid-spec
                  (map (fn [[a b]] [(.toString a) b]))
                  (into {})))))

    (testing "should build up tids"
      (is (= [{:tid     :clienta
               :columns {:a (t/date-time 2016 5 18)
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:tid     :clientb
               :columns {:a (t/date-time 2016 5 12)
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a (t/date-time 2016 5 10)
                         :b -100.11
                         :d "unknown-stuff"}}]
             (csv-pars/with-tids with-cols tid-spec :d))))))
