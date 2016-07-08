(ns finalytics.charts.barchart
  (:require cljsjs.d3
            [finalytics.chart.utils :as utils]))

(defn bar-chart-sort [transactions]
  (let [positive (filter #(>= (get-in % [:columns :value]) 0) transactions)
        negative (filter #(< (get-in % [:columns :value]) 0) transactions)]
    (concat
      (reverse
        (sort-by #(get-in % [:columns :value]) positive))
      (sort-by #(get-in % [:columns :value]) negative))))

(defn append-day-axis [group-container bar-width axis-pos x axis-height day]
  (-> (.append group-container "text")
      (.attr "width" bar-width)
      (.attr "height" axis-height)
      (.attr "x" (+ x (/ bar-width 2)))
      (.attr "y" axis-pos)
      (.attr "text-anchor" "middle")
      (.attr "alignment-baseline" "central")
      (.attr "style" (str "font-size:" (* axis-height (/ 4 5)) "px"))
      (.html day)))

(defn append-daydata [svg-container [day transactions] {:keys [bar-width chart-height yscale axis-height]}]
  (let [x (* (- day 1) bar-width)
        axis-pos (/ chart-height 2)]
    (loop [the-transactions (bar-chart-sort transactions)
           positive-offset 0
           negativ-offset 0]
      (when-not (empty? the-transactions)
        (let [{:keys [columns color]} (first the-transactions)
              the-color (or color "rgb(0,0,255)")
              the-val (yscale (:value columns))
              y (if (< the-val 0)
                  (+ axis-pos negativ-offset (/ axis-height 2))
                  (- axis-pos positive-offset the-val (/ axis-height 2)))
              group-container (.append svg-container "g")]
          (append-day-axis group-container bar-width axis-pos x axis-height day)
          (-> (.append group-container "rect")
              (.attr "x" x)
              (.attr "y" y)
              (.attr "style" (str "fill: " the-color ";stroke-width:1;stroke:rgb(0,0,0)"))
              (.attr "data-val" columns)
              (.attr "width" bar-width)
              (.attr "height" (Math/abs the-val)))
          (if (< the-val 0)
            (recur (rest the-transactions) positive-offset (+ negativ-offset (Math/abs the-val)))
            (recur (rest the-transactions) (+ positive-offset the-val) negativ-offset)))))))

(defn append-x-axis [svg-container {:keys [chart-width chart-height axis-height]}]
  (-> (.append svg-container "rect")
      (.attr "x" 0)
      (.attr "y" (- (/ chart-height 2) (/ axis-height 2)))
      (.attr "width" chart-width)
      (.attr "height" axis-height)
      (.attr "fill-opacity" 0)
      (.attr "style" "stroke-width:1;stroke:rgb(0,0,0)")))

(defn bar-chart-conf [{:keys [max-withdrawal-per-day max-receival-per-day]}]
  (let [drawing-height 500
        top-bottom-space 50
        bar-width (/ drawing-height 10)
        axis-height (/ bar-width 3)]
    {:drawing-height   drawing-height
     :bar-width        bar-width
     :chart-width      (* 31 bar-width)
     :chart-height     (+ drawing-height (* 2 top-bottom-space) axis-height)
     :top-bottom-space top-bottom-space
     :axis-height      axis-height
     :yscale           (utils/d3YScale (max max-receival-per-day max-withdrawal-per-day) drawing-height)}))

(defn bar-chart [container csv-data meta-data]
  (let [bconf (bar-chart-conf meta-data)]
    (utils/render-chart
      container csv-data
      (fn [_ container {:keys [days]}]
        (let [svg-container (utils/append-svg-container container bconf)]
          (append-x-axis svg-container bconf)
          (doseq [day-entry (utils/with-missing-days-as-empty days)]
            (append-daydata svg-container day-entry bconf)))))))
