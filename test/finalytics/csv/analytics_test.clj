(ns finalytics.csv.analytics-test
  (:require
    [finalytics.csv.analytics :as atic]
    [clojure.test :refer :all]
    [clj-time.core :as t]))

(deftest determine-all-months
  (testing "should determine all months in order"
    (is (= {2016 {2 {18 [{:tid             :clienta
                          :classifications [:gas :food]
                          :columns         {:a (t/date-time 2016 2 18)
                                            :b "bar"}}]}
                  4 {12 [{:tid     :clientb
                          :columns {:a (t/date-time 2016 4 12)
                                    :b "baz"}}]}

                  3 {10 [{:columns {:a (t/date-time 2016 3 10)
                                    :b "baf"}}]}}}
           (atic/as-date-map [{:tid             :clienta
                               :classifications [:gas :food]
                               :columns         {:a (t/date-time 2016 2 18)
                                                 :b "bar"}}
                              {:tid     :clientb
                               :columns {:a (t/date-time 2016 4 12)
                                         :b "baz"}}
                              {:columns {:a (t/date-time 2016 3 10)
                                         :b "baf"}}]
                             :a)))))

(deftest transactions-by-year
  (testing "should return transactions for years"
    (is (= {2016 [{:tid             :clienta
                   :classifications [:gas :food]
                   :columns         {:a (t/date-time 2016 2 18)
                                     :b "bar"}}
                  {:tid     :clientb
                   :columns {:a (t/date-time 2016 4 12)
                             :b "baz"}}
                  {:columns {:a (t/date-time 2016 4 10)
                             :b "baf"}}]}
           (atic/by-year {2016 {2 {18 [{:tid             :clienta
                                        :classifications [:gas :food]
                                        :columns         {:a (t/date-time 2016 2 18)
                                                          :b "bar"}}]}
                                4 {12 [{:tid     :clientb
                                        :columns {:a (t/date-time 2016 4 12)
                                                  :b "baz"}}]
                                   10 [{:columns {:a (t/date-time 2016 4 10)
                                                  :b "baf"}}]}}})))))

(deftest transactions-by-month
  (testing "should return transactions for months"
    (is (= {2016 {2 [{:tid             :clienta
                      :classifications [:gas :food]
                      :columns         {:a (t/date-time 2016 2 18)
                                        :b "bar"}}]
                  4 [{:tid     :clientb
                      :columns {:a (t/date-time 2016 4 12)
                                :b "baz"}}
                     {:columns {:a (t/date-time 2016 4 10)
                                :b "baf"}}]}}
           (atic/by-month {2016 {2 {18 [{:tid             :clienta
                                         :classifications [:gas :food]
                                         :columns         {:a (t/date-time 2016 2 18)
                                                           :b "bar"}}]}
                                 4 {12 [{:tid     :clientb
                                         :columns {:a (t/date-time 2016 4 12)
                                                   :b "baz"}}]
                                    10 [{:columns {:a (t/date-time 2016 4 10)
                                                   :b "baf"}}]}}})))))


(deftest aggregation
  (testing "should aggregate on day level"
    (is (= {2016 {2 {18 1}
                  4 {12 3
                     10 2}}}
          (atic/aggregate
            {2016 {2 {18 [{:tid             :clienta
                           :classifications [:gas :food]
                           :columns         {:a (t/date-time 2016 2 18)
                                             :b 1}}]}
                   4 {12 [{:tid     :clientb
                           :columns {:a (t/date-time 2016 4 12)
                                     :b 3}}]
                      10 [{:columns {:a (t/date-time 2016 4 10)
                                     :b 2}}]}}}
            :b))))

  (testing "should aggregate on month level"
    (is (= {2016 {2 1
                  4 5}}
          (atic/aggregate
            {2016 {2 [{:tid             :clienta
                       :classifications [:gas :food]
                       :columns         {:a (t/date-time 2016 2 18)
                                         :b 1}}]
                   4 [{:tid     :clientb
                       :columns {:a (t/date-time 2016 4 12)
                                 :b 3}}
                      {:columns {:a (t/date-time 2016 4 10)
                                 :b 2}}]}}
            :b))))

  (testing "should aggregate on year level"
    (is (= {2016 6}
          (atic/aggregate
            {2016 [{:tid             :clienta
                    :classifications [:gas :food]
                    :columns         {:a (t/date-time 2016 2 18)
                                      :b 1}}
                   {:tid     :clientb
                    :columns {:a (t/date-time 2016 4 12)
                              :b 3}}
                   {:columns {:a (t/date-time 2016 4 10)
                              :b 2}}]}
            :b)))))