(ns finalytics.csv.csv-loader
  (:require
    [finalytics.csv.parsing :as pars]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]))

(defn load-csv-data []
  (log/info "-> loading csv-data")
  (let [data-spec (pars/load-data-spec "resources/public/data/spec.edn")
        tid-spec (pars/load-tid-spec "resources/public/data/tid-spec.edn")]
    (-> (pars/load-csv "resources/public/data/csv")
        (pars/with-columns data-spec)
        (pars/with-tids tid-spec :client))))

(defstate csv-data :start (load-csv-data))
