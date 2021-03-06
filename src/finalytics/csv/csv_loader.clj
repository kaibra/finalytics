(ns finalytics.csv.csv-loader
  (:require
    [finalytics.csv.parsing :as pars]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]
    [mount.core :as mount]))

(defstate spec-file :start (atom "test-resources/demo-data/spec.edn"))
(defstate data-folder :start (atom "test-resources/demo-data/csv"))

(defn load-csv-data []
  (log/info "-> loading csv-data")
  (let [csv-data (pars/load-parsed-csv-data @spec-file @data-folder)]
    {:csv-data csv-data
     :meta-data (pars/meta-data csv-data)}))

(defstate csv-data :start (load-csv-data))

(defn update-and-reload-csv-data [csv-dir spec-dir]
  (reset! data-folder csv-dir)
  (reset! spec-file spec-dir)
  (mount/stop #'csv-data)
  (mount/start #'csv-data))
