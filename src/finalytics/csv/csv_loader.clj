(ns finalytics.csv.csv-loader
  (:require
    [finalytics.csv.parsing :as pars]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]))

(defn load-csv-data [& {:keys [spec-folder data-folder]}]
  (log/info "-> loading csv-data")
  (let [col-spec (pars/load-columns-spec (str spec-folder "/columns.edn"))
        tid-spec (pars/load-tid-spec (str spec-folder "/tids.edn"))
        class-spec (pars/load-class-spec (str spec-folder "/classifications.edn"))]
    (-> (pars/load-csv data-folder)
        (pars/with-columns col-spec)
        (pars/with-tids tid-spec :client)
        (pars/with-classification class-spec))))

(defstate csv-data :start
          (load-csv-data
            :spec-folder  "resources/public/data/spec"
            :data-folder "resources/public/data/csv"))
