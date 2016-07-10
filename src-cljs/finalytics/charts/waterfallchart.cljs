(ns finalytics.charts.waterfallchart
  (:require cljsjs.d3
            [cemerick.url :as url]
            [finalytics.chart.utils :as utils]))

(defn waterfall-range [transactions]
  (loop [all-transactions transactions
         pos (- (count transactions) 1)
         value-range []]
    (if (empty? all-transactions)
      value-range
      (let [transaction (first all-transactions)
            transaction-value (get-in transaction [:columns :value])
            last-waterfall-value (or (:waterfall-value (last value-range)) 0)]
        (recur (rest all-transactions)
               (dec pos)
               (conj value-range {:waterfall-value (+ last-waterfall-value transaction-value)
                                  :pos             pos
                                  :transaction     transaction}))))))

(defn waterfall-chart-conf [all-transactions last-balance]
  (let [waterfall-range (waterfall-range all-transactions)
        waterfall-value-range (map :waterfall-value waterfall-range)
        chart-height 500
        chart-width (* chart-height 3)
        bar-width (/ chart-width (count all-transactions))
        min-val (Math/abs (apply min waterfall-value-range))
        max-val (Math/abs (apply max waterfall-value-range))]
    {:waterfall-range waterfall-range
     :bar-width       bar-width
     :chart-width     chart-width
     :chart-height    chart-height
     :yscale          (utils/d3YScale (+ (Math/max min-val max-val) (Math/abs last-balance))
                                      chart-height)}))

(defn append-x-axis [svg-container chart-height chart-width]
  (-> (.append svg-container "rect")
      (.attr "x" 0)
      (.attr "y" (/ chart-height 2))
      (.attr "width" chart-width)
      (.attr "height" 1)))

(defn render-waterfall [svg-container last-balance {:keys [waterfall-range bar-width chart-height chart-width yscale]}]
  (append-x-axis svg-container chart-height chart-width)
  (loop [the-range waterfall-range
         current-balance last-balance]
    (if (empty? the-range)
      current-balance
      (let [{:keys [pos transaction]} (first the-range)
            {:keys [columns color]} transaction
            transaction-value (get-in transaction [:columns :value])
            transaction-value-scaled (yscale transaction-value)
            the-color (or color "rgb(0,0,255)")
            current-balance-scaled (- (/ chart-height 2) (yscale current-balance))
            y (if (< transaction-value-scaled 0)
                (+ current-balance-scaled transaction-value-scaled)
                current-balance-scaled)]
        (-> (.append svg-container "rect")
            (.attr "x" (* pos bar-width))
            (.attr "y" y)
            (.attr "style" (str "fill: " the-color ";stroke-width:1;stroke:rgb(0,0,0)"))
            (.attr "data-val" columns)
            (.attr "width" bar-width)
            (.attr "height" (Math/abs transaction-value-scaled)))
        (recur (rest the-range) (- current-balance transaction-value))))))

(defn append-current-balance-form [container]
  (let [cb-id "currentbalance"
        current-balance (if-let [cb (get-in (url/url (-> js/window .-location .-href))
                                            [:query cb-id])]
                          (js/parseFloat cb)
                          0)
        form-container (-> (.append container "form")
                           (.attr "class" "form-inline")
                           (.attr "role" "form"))]
    (doto (-> (.append form-container "div")
              (.attr "class" "form-group"))
      (-> (.append "label")
          (.attr "for" cb-id)
          (.html "Current balance:"))
      (-> (.append "input")
          (.attr "type" "text")
          (.attr "name" cb-id)
          (.attr "id" cb-id)
          (.attr "value" current-balance)
          (.attr "class" "form-control")))
    (-> (.append form-container "button")
        (.attr "type" "submit")
        (.attr "class" "btn btn-default")
        (.html "Render charts"))
    current-balance))

(defn waterfall-chart [container csv-data _]
  (let [current-balance (append-current-balance-form container)]
    (utils/render-chart
      container csv-data
      (fn [last-month-start-balance c {:keys [year month days]}]
        (let [last-balance (or last-month-start-balance current-balance)
              all-transactions (reverse (flatten (vals days)))
              cconf (waterfall-chart-conf all-transactions last-balance)
              svg-container (utils/append-svg-container c cconf)]
          (render-waterfall svg-container last-balance cconf))))))
