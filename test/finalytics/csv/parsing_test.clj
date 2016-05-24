(ns finalytics.csv.parsing-test
  (:require [clojure.test :refer :all]
            [finalytics.csv.parsing :as csv-pars]
            [clojure.java.io :as io]))

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
             (csv-pars/with-named-columns csv-data
                                          [:a :b :c]))))))
