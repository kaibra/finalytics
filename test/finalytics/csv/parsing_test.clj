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
                                         {:asian-food  [:mister-foo]})))))

(deftest loading-the-column-spec
  (testing "should load the column spec"
    (is (= [[:a
             {:format "dd.MM.yyyy"
              :type   :date}]
            [:b
             {:locale Locale/GERMAN
              :type   :number}]
            nil
            :client]
           (csv-pars/load-columns-spec "test-resources/data/spec/columns.edn")))))

(deftest loading-the-tid-spec
  (testing "should load the tid spec"
    (is (= [:clienta :clientb]
           (vals (csv-pars/load-tid-spec "test-resources/data/spec/tids.edn"))))))

(deftest loading-the-classification-spec
  (testing "should load the classification spec"
    (is (= {:food [:clienta
                   :clientb]
            :gas  [:clienta]}
           (csv-pars/load-class-spec "test-resources/data/spec/classifications.edn")))))


(deftest loading-complete-parsed-csv-data
  (testing "should load the whole test-data set"
    (is (= [{:classifications [:gas :food]
             :columns         {:a      {:day   18
                                        :month 5
                                        :year  2016}
                               :b      -16.13
                               :client "Thank you says clienta"}
             :tid             :clienta}
            {:classifications [:food]
             :columns         {:a      {:day   12
                                        :month 5
                                        :year  2016}
                               :b      100000.1122
                               :client "This is a clientb transaction"}
             :tid             :clientb}
            {:columns {:a      {:day   10
                                :month 5
                                :year  2016}
                       :b      -100.11
                       :client "unknown-stuff"}}]
           (csv-pars/load-parsed-csv-data "test-resources/data/spec" "test-resources/data/csv-b")))))
