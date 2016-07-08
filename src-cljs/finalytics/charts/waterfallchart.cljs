(ns finalytics.charts.waterfallchart
  (:require cljsjs.d3
            [finalytics.chart.utils :as utils]))

(defn waterfall-range [transactions]
  (loop [all-transactions transactions
         pos 0
         value-range []]
    (if (empty? all-transactions)
      value-range
      (let [transaction (first all-transactions)
            transaction-value (get-in transaction [:columns :value])
            last-waterfall-value (or (:waterfall-value (last value-range)) 0)]
        (recur (rest all-transactions)
               (inc pos)
               (conj value-range {:waterfall-value (+ last-waterfall-value transaction-value)
                                  :pos             pos
                                  :transaction     transaction}))))))

(defn waterfall-chart-conf [all-transactions]
  (let [waterfall-range (waterfall-range all-transactions)
        waterfall-value-range (map :waterfall-value waterfall-range)
        drawing-height 500
        bar-width (/ drawing-height 10)
        top-bottom-space 50]
    {:waterfall-range waterfall-range
     :bar-width       bar-width
     :chart-width     (* bar-width (count all-transactions))
     :chart-height    (+ drawing-height (* 2 top-bottom-space))
     :yscale          (utils/d3YScale (Math/max (Math/abs (apply min waterfall-value-range))
                                                (Math/abs (apply max waterfall-value-range)))
                                      drawing-height)}))

(defn append-x-axis [svg-container chart-height chart-width]
  (-> (.append svg-container "rect")
      (.attr "x" 0)
      (.attr "y" (/ chart-height 2))
      (.attr "width" chart-width)
      (.attr "height" 1)))

(defn render-waterfall [svg-container {:keys [waterfall-range bar-width chart-height chart-width yscale]}]
  (append-x-axis svg-container chart-height chart-width)
  (loop [the-range waterfall-range
         last-axis-pos (/ chart-height 2)]
    (when-not (empty? the-range)
      (let [{:keys [pos transaction]} (first the-range)
            {:keys [columns color]} transaction
            transaction-value (yscale (get-in transaction [:columns :value]))
            the-color (or color "rgb(0,0,255)")
            y (if (< transaction-value 0)
                last-axis-pos
                (- last-axis-pos transaction-value))]
        (-> (.append svg-container "rect")
            (.attr "x" (* pos bar-width))
            (.attr "y" y)
            (.attr "style" (str "fill: " the-color ";stroke-width:1;stroke:rgb(0,0,0)"))
            (.attr "data-val" columns)
            (.attr "width" bar-width)
            (.attr "height" (Math/abs transaction-value)))
        (recur (rest the-range) (- last-axis-pos transaction-value))))))

(defn waterfall-chart [container csv-data _]
  (utils/render-chart
    container csv-data
    (fn [c {:keys [days]}]
      (let [all-transactions (flatten (vals days))
            cconf (waterfall-chart-conf all-transactions)
            svg-container (utils/append-svg-container c cconf)]
        (render-waterfall svg-container cconf)))))
