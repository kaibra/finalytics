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
      (is (= [{:columns {:a {:year 2016
                             :month 5
                             :day 18}
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 12}
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 10}
                         :b -100.11
                         :d "unknown-stuff"}}]
             (csv-pars/with-columns csv-data
                                    [[:a {:type   :date
                                          :format "dd.MM.yyyy"}]
                                     [:b {:type   :number
                                          :locale Locale/GERMAN}]
                                     nil
                                     :d]))))))

(deftest reading-csv-data
  (let [classification-column :d
        columns-spec (csv-pars/load-columns-spec "test-resources/data/spec/columns.edn")
        tids-spec (csv-pars/load-tid-spec "test-resources/data/spec/tids.edn")
        classifications-spec (csv-pars/load-class-spec "test-resources/data/spec/classifications.edn")
        csv-with-cols (csv-pars/with-columns (csv-pars/load-csv "test-resources/data/csv-b") columns-spec)
        csv-with-tids (csv-pars/with-tids csv-with-cols tids-spec classification-column)
        csv-with-classifications (csv-pars/with-classification csv-with-tids classifications-spec)]

    (testing "should read the data-spec"
      (is (= [[:a {:type   :date
                   :format "dd.MM.yyyy"}]
              [:b {:type   :number
                   :locale java.util.Locale/GERMAN}]
              nil
              :d]
             columns-spec)))

    (testing "should build up columns"
      (is (= [{:columns {:a {:year 2016
                             :month 5
                             :day 18}
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 12}
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 10}
                         :b -100.11
                         :d "unknown-stuff"}}]
             csv-with-cols)))

    (testing "should load the tid file"
      (is (= {"^.*clienta.*" :clienta
              "^.*clientb.*" :clientb}
             (->> tids-spec
                  (map (fn [[a b]] [(.toString a) b]))
                  (into {})))))

    (testing "should build up tids"
      (is (= [{:tid     :clienta
               :columns {:a {:year 2016
                             :month 5
                             :day 18}
                         :b -16.13
                         :d "Thank you says clienta"}}
              {:tid     :clientb
               :columns {:a {:year 2016
                             :month 5
                             :day 12}
                         :b 100000.1122
                         :d "This is a clientb transaction"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 10}
                         :b -100.11
                         :d "unknown-stuff"}}]
             csv-with-tids)))

    (testing "should read the classifications-spec"
      (is (= {:food [:clienta
                     :clientb]
              :gas  [:clienta]}
             classifications-spec)))

    (testing "should apply the classifications-spec for classification"
      (is (= [{:tid             :clienta
               :classifications [:gas :food]
               :columns         {:a {:year 2016
                                     :month 5
                                     :day 18}
                                 :b -16.13
                                 :d "Thank you says clienta"}}
              {:tid             :clientb
               :classifications [:food]
               :columns         {:a {:year 2016
                                     :month 5
                                     :day 12}
                                 :b 100000.1122
                                 :d "This is a clientb transaction"}}
              {:columns {:a {:year 2016
                             :month 5
                             :day 10}
                         :b -100.11
                         :d "unknown-stuff"}}]
             csv-with-classifications)))))
