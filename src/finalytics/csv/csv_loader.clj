(ns finalytics.csv.csv-loader
  (:require
    [finalytics.csv.parsing :as pars]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]))

(defn load-csv-data []
  (log/info "-> loading csv-data")
  (let [col-spec (pars/load-data-spec "resources/public/data/col-spec.edn")
        tid-spec (pars/load-tid-spec "resources/public/data/tid-spec.edn")
        class-spec (pars/load-class-spec "resources/public/data/class-spec.edn")]
    (-> (pars/load-csv "resources/public/data/csv")
        (pars/with-columns col-spec)
        (pars/with-tids tid-spec :client)
        (pars/with-classification class-spec))))

(defstate csv-data :start (load-csv-data))
