(ns finalytics.csv.parsing
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.format :as f]
            [clj-time.core :as t]
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

(defn- to-column [column-spec content]
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
                               {:year (t/year pdate)
                                :month (t/month pdate)
                                :day (t/day pdate)})])))))

(defn load-columns-spec [data-spec-file]
  (->> (slurp data-spec-file)
       (edn/read-string {:readers {'locale eval}})))

(defn load-tid-spec [file-path]
  (->> (edn/read-string (slurp file-path))
      (map (fn [[pattern classification]] [(re-pattern pattern) classification]))
      (into {})))


(def col-key :columns)
(def tid-key :tid)

(defn with-columns [csv-data column-names]
  (map
    (fn [transaction]
      {col-key (->> (map to-column column-names transaction) (into {}))})
    csv-data))

(defn tid-transaction [tid-specs tid-col transaction]
  (let [ct-val (get-in transaction [col-key tid-col])]
    (loop [specs tid-specs]
      (if (empty? specs)
        transaction
        (let [[c-pattern classification-name] (first specs)]
          (if-not (nil? (re-matches c-pattern ct-val))
            (assoc transaction :tid classification-name)
            (recur (rest specs))))))))

(defn with-tids [csv-data tid-specs tid-col]
  (map (partial tid-transaction tid-specs tid-col) csv-data))

(defn load-class-spec [file-path]
  (edn/read-string (slurp file-path)))

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
