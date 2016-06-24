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

(defn with-columns [csv-data column-names]
  (map
    (fn [row]
      {col-key (->> (map to-row-with-column column-names row) (into {}))})
    csv-data))

(defn tid-transaction [tid-specs tid-col transaction]
  (let [ct-val (get-in transaction [col-key tid-col])]
    (loop [specs tid-specs]
      (if (empty? specs)
        transaction
        (let [[c-pattern classification-name] (first specs)]
          (if-not (nil? (re-matches (re-pattern c-pattern) ct-val))
            (assoc transaction :tid classification-name)
            (recur (rest specs))))))))

(defn with-tids [csv-data tid-specs tid-col]
  (map (partial tid-transaction tid-specs tid-col) csv-data))

(defn classify-transaction [[classification-name tids] {:keys [tid] :as transaction}]
  (if (some #(= tid %) tids)
    (update transaction :classifications conj classification-name)
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


(defn group-data-by [data the-path]
  (wlk/postwalk
    (fn [d]
      (if (and
            (coll? d)
            (not (nil? (:columns (first d)))))
        (group-by #(get-in % the-path) d)
        d))
    data))

(defn group-by-date-column [rows date-column]
  (let [date-path [:columns date-column]]
    (-> rows
        (group-data-by (conj date-path :year))
        (group-data-by (conj date-path :month))
        (group-data-by (conj date-path :day)))))

(defn load-parsed-csv-data [spec-file data-folder]
  (let [{:keys [classifications
                tid-column
                date-column
                tids
                columns]} (edn/read-string {:readers {'locale eval}} (slurp spec-file))]
    (-> (load-csv data-folder)
        (with-columns columns)
        (with-tids tids tid-column)
        (with-classification classifications)
        (sorted-rows date-column)
        (group-by-date-column date-column))))
