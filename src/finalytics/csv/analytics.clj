(ns finalytics.csv.analytics
  (:require [clj-time.core :as t]))


(defn- assoc-transaction-by-date [date-col result transaction]
  (let [date-val (get-in transaction [:columns date-col])
        year (t/year date-val)
        month (t/month date-val)
        day (t/day date-val)]
    (update-in result [year month day] conj transaction)))

(defn as-date-map [csv-data date-col]
  (reduce
    (partial assoc-transaction-by-date date-col)
    {}
    csv-data))

(defn by-year [date-map]
  (into {}
        (map
          (fn [[year months]]
            [year (mapcat
                    (fn [[_ days]]
                      (mapcat
                        (fn [[_ transactions]]
                          transactions)
                        days))
                    months)])
          date-map)))

(defn by-month [date-map]
  (into {}
        (map
          (fn [[year months]]
            [year (into {}
                        (map
                          (fn [[month days]]
                            [month
                             (mapcat
                               (fn [[_ transactions]]
                                 transactions)
                               days)])
                          months))])
          date-map)))