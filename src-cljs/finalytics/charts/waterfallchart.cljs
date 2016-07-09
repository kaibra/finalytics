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

(defn waterfall-chart-conf [all-transactions last-month-result]
  (let [waterfall-range (waterfall-range all-transactions)
        waterfall-value-range (map :waterfall-value waterfall-range)
        chart-height 500
        chart-width (* chart-height 3)
        bar-width (/ chart-width (count all-transactions))]
    {:waterfall-range waterfall-range
     :bar-width       bar-width
     :chart-width     chart-width
     :chart-height    chart-height
     :yscale          (utils/d3YScale (Math/max (Math/abs (+ (apply min waterfall-value-range) last-month-result))
                                                (Math/abs (+ (apply max waterfall-value-range) last-month-result)))
                                      chart-height)}))

(defn append-x-axis [svg-container chart-height chart-width]
  (-> (.append svg-container "rect")
      (.attr "x" 0)
      (.attr "y" (/ chart-height 2))
      (.attr "width" chart-width)
      (.attr "height" 1)))

(defn render-waterfall [svg-container last-month-result {:keys [waterfall-range bar-width chart-height chart-width yscale]}]
  (append-x-axis svg-container chart-height chart-width)
  (loop [the-range waterfall-range
         last-axis-pos (or last-month-result 0)]
    (if (empty? the-range)
      last-axis-pos
      (let [{:keys [pos transaction]} (first the-range)
            {:keys [columns color]} transaction
            transaction-value (get-in transaction [:columns :value])
            transaction-value-scaled (yscale transaction-value)
            the-color (or color "rgb(0,0,255)")
            last-axis-pos-scaled (- (/ chart-height 2) (yscale last-axis-pos))
            y (if (< transaction-value-scaled 0)
                last-axis-pos-scaled
                (- last-axis-pos-scaled transaction-value-scaled))]
        (-> (.append svg-container "rect")
            (.attr "x" (* pos bar-width))
            (.attr "y" y)
            (.attr "style" (str "fill: " the-color ";stroke-width:1;stroke:rgb(0,0,0)"))
            (.attr "data-val" columns)
            (.attr "width" bar-width)
            (.attr "height" (Math/abs transaction-value-scaled)))
        (recur (rest the-range) (+ last-axis-pos transaction-value))))))

(defn waterfall-chart [container csv-data _]
  (utils/render-chart
    container csv-data
    (fn [last-month-result c {:keys [days]}]
      (let [all-transactions (flatten (vals days))
            cconf (waterfall-chart-conf all-transactions last-month-result)
            svg-container (utils/append-svg-container c cconf)]
        (render-waterfall svg-container last-month-result cconf)))))
