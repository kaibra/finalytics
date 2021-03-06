(ns finalytics.csv.parsing
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.walk :as wlk]
            [clojure.edn :as edn])
  (:import (java.text NumberFormat)))

(defn read-csv-lines [csv-file]
  (with-open [rdr (io/reader csv-file)]
    (into [] (line-seq rdr))))

(defn replace-quotes [c]
  (let [quote "\""]
    (if (and
          (str/starts-with? c quote)
          (str/ends-with? c quote))
      (.substring c 1 (- (count c) 1))
      c)))

(defn parse-single-csv-line [sep line]
  (let [splitted (str/split line (re-pattern sep))]
    (->> splitted
         (map str/trim)
         (map replace-quotes)
         (map str/trim))))

(defn parse-csv-data [line-set sep]
  (map (partial parse-single-csv-line sep) line-set))

(defn load-csv
  ([folder-name]
   (load-csv folder-name ";"))
  ([folder-name sep]
   (loop [all-files (rest (file-seq (io/file folder-name)))
          lines #{}]
     (if (empty? all-files)
       (parse-csv-data lines sep)
       (recur (rest all-files) (into lines (read-csv-lines (first all-files))))))))

(defn- to-row-with-column [column-spec content]
  (when-not (nil? column-spec)
    (if-not (vector? column-spec)
      [column-spec content]
      (let [[column-name {:keys [type format locale]}] column-spec]
        (case type
          :number [column-name (-> (NumberFormat/getInstance locale)
                                   (.parse content)
                                   (.doubleValue))]
          :string [column-name content]
          :date [column-name (let [pdate (f/parse (f/formatter format) content)]
                               {:year  (t/year pdate)
                                :month (t/month pdate)
                                :day   (t/day pdate)})])))))


(def col-key :columns)
(def tid-key :tid)

(defn- column [transaction col-name]
  (get-in transaction [col-key col-name]))

(defn with-columns [csv-data column-names]
  (map
    (fn [row]
      {col-key (->> (map to-row-with-column column-names row) (into {}))})
    csv-data))

(defn transaction-matches-tid-spec? [transaction tid-spec]
  (every?
    (fn [[tid-col tid-regex]]
      (let [col-val (column transaction tid-col)
            mtaching-result (re-matches (re-pattern tid-regex) col-val)]
        (not
          (nil? mtaching-result))))
    tid-spec))

(defn tid-transaction [tid-specs transaction]
  (loop [specs tid-specs]
    (if (empty? specs)
      transaction
      (let [[tid-spec classification-name] (first specs)]
        (if (transaction-matches-tid-spec? transaction tid-spec)
          (assoc transaction :tid classification-name)
          (recur (rest specs)))))))

(defn with-tids [csv-data tid-specs]
  (map (partial tid-transaction tid-specs) csv-data))

(defn classify-transaction [[classification-name {:keys [tids color]}] {:keys [tid] :as transaction}]
  (if (some #(= tid %) tids)
    (-> transaction
        (assoc :classification classification-name)
        (assoc :color color))
    transaction))

(defn fully-classified-transaction [class-spec transaction]
  (loop [specs class-spec
         t transaction]
    (if (empty? specs)
      t
      (recur (rest specs) (classify-transaction (first specs) t)))))

(defn with-classification [csv-data class-spec]
  (map
    (partial fully-classified-transaction class-spec)
    csv-data))

(defn sorted-rows [rows sort-column]
  (sort-by
    (fn [{:keys [columns] :as row}]
      (let [sc (get columns sort-column)
            {:keys [day year month]} sc]
        (if (or
              (nil? day)
              (nil? year)
              (nil? month))
          sc
          (format "%04d%02d%02d" year month day))))
    rows))


(defn data-item? [item]
  (not (nil? (:columns item))))

(defn data-item-seq? [d]
  (and
    (coll? d)
    (data-item? (first d))))

(defn group-data-by [data the-path]
  (wlk/postwalk
    (fn [d]
      (if (data-item-seq? d)
        (group-by #(get-in % the-path) d)
        d))
    data))

(defn group-by-date-column [rows date-column]
  (let [date-path [:columns date-column]]
    (-> rows
        (group-data-by (conj date-path :year))
        (group-data-by (conj date-path :month))
        (group-data-by (conj date-path :day)))))

(defn meta-data [data]
  (let [nr-transactions (atom 0)
        max-per-day (atom 0)
        max-abs-per-day (atom 0)
        max-withdrawal-per-day (atom 0)
        max-receival-per-day (atom 0)]
    (wlk/postwalk
      (fn [d]
        (when (data-item-seq? d)
          (swap! nr-transactions + (count d))
          (let [total-sum (->> (map #(get-in % [:columns :value]) d)
                               (apply +))
                receival-sum (->> (map #(get-in % [:columns :value]) d)
                                  (filter #(>= 0 %))
                                  (apply +))
                withdrawal-sum (->> (map #(get-in % [:columns :value]) d)
                                    (filter #(< 0 %))
                                    (apply +))]
            (when (> total-sum @max-per-day)
              (reset! max-per-day total-sum))

            (when (> (Math/abs total-sum) @max-abs-per-day)
              (reset! max-abs-per-day (Math/abs total-sum)))

            (when (> withdrawal-sum @max-receival-per-day)
              (reset! max-receival-per-day withdrawal-sum))

            (when (> (Math/abs receival-sum) @max-withdrawal-per-day)
              (reset! max-withdrawal-per-day (Math/abs receival-sum)))))
        d)
      data)
    {:max-per-day            @max-per-day
     :max-abs-per-day        @max-abs-per-day
     :max-withdrawal-per-day @max-withdrawal-per-day
     :max-receival-per-day   @max-receival-per-day
     :nr-transactions        @nr-transactions}))

(defn load-parsed-csv-data [spec-file data-folder]
  (let [{:keys [classifications
                date-column
                tids
                columns]} (edn/read-string {:readers {'locale eval}} (slurp spec-file))]
    (-> (load-csv data-folder)
        (with-columns columns)
        (with-tids tids)
        (with-classification classifications)
        (sorted-rows date-column)
        (group-by-date-column date-column))))
