(ns finalytics.core
  (:require
    [kaibra.system :as mount-ms]
    [mount.core :as tn]
    [clojure.tools.logging :as log]
    [finalytics.csv.csv-loader :as csv-loader]
    [finalytics.server :as server])
  (:gen-class))

(def custom-states
  [#'server/server
   #'csv-loader/csv-data])

(defn start []
  (apply mount-ms/start-with-states custom-states))

(defn stop []
  (apply mount-ms/stop custom-states))

(defn refresh []
  (stop)
  (start))

(defn -main [& args]
  (log/info "Starting MS-EXAMPLE")
  (start))
