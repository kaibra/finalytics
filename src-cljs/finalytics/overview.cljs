(ns finalytics.overview
  (:require cljsjs.d3
            [finalytics.charts.barchart :as bc]))

(enable-console-print!)

(println "Hello from ClojureScript! using d3 version: " js/d3.version)

(defn register-all-listeners []
  (println "Register all")

  (bc/bar-chart js/csvdata))

(register-all-listeners)
