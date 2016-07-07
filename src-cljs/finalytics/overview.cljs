(ns finalytics.overview
  (:require cljsjs.d3
            [finalytics.charts.barchart :as bc]
            [finalytics.charts.waterfallchart :as wc]
            [cljs.reader :as edn]))

(def chart-spec
  [["#barchart" bc/bar-chart]
   ["#waterfallchart" wc/waterfall-chart]])

(defn render-if-container-present [id call-fn & args]
  (when-let [container (js/d3.select id)]
    (when-not (nil? (first (first container)))
      (apply call-fn container args))))

(defn render-charts []
  (println "Rendering all charts with D3.js version " js/d3.version)
  (let [{:keys [csv-data meta-data]} (edn/read-string js/csvdata)]
    (doseq [[id the-fn] chart-spec]
      (render-if-container-present id the-fn csv-data meta-data))))

(enable-console-print!)
(render-charts)
