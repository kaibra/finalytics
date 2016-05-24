(ns finalytics.csv.parsing
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.format :as f]
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
          :date [column-name (f/parse (f/formatter format) content)])))))

(defn with-columns [csv-data column-names]
  (map
    #(->> (map to-column column-names %)
          (into {}))
    csv-data))

(defn read-data-spec [data-spec-file]
  (->> (slurp data-spec-file)
       (edn/read-string {:readers {'locale eval}})))
