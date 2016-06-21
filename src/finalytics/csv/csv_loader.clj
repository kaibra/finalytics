(ns finalytics.csv.csv-loader
  (:require
    [finalytics.csv.parsing :as pars]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]
    [mount.core :as mount]))

(defstate spec-folder :start (atom "data/spec"))
(defstate data-folder :start (atom "data/csv"))

(defn load-csv-data []
  (log/info "-> loading csv-data")
  (pars/load-parsed-csv-data @spec-folder @data-folder))

(defstate csv-data :start (load-csv-data))

(defn update-and-reload-csv-data [csv-dir spec-dir]
  (reset! data-folder csv-dir)
  (reset! spec-folder spec-dir)
  (mount/stop #'csv-data)
  (mount/start #'csv-data))
