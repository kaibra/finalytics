(ns finalytics.charts.barchart
  (:require cljsjs.d3
            [cljs.reader :as edn]))

(defn d3YScale [max-value drawing-height]
  (let [half-drawing-height (/ drawing-height 2)]
    (-> (js/d3.scale.linear)
        (.domain (array (- max-value) max-value))
        (.range (array (- half-drawing-height) half-drawing-height)))))

(defn add-svg-background [svg-container]
  (-> svg-container
      (.append "rect")
      (.attr "fill" "blue")
      (.attr "width" "100%")
      (.attr "height" "100%")))

(defn append-svg-container [container [width height]]
  (doto (-> container
            (.append "svg")
            (.attr "width" "100%")
            (.attr "height" "100%")
            (.attr "viewBox" (str "0 0 " width " " height)))
    (add-svg-background)))

(defn bar-chart-sort [transactions]
  (let [positive (filter #(>= (get-in % [:columns :value]) 0) transactions)
        negative (filter #(< (get-in % [:columns :value]) 0) transactions)]
    (concat
      (reverse
        (sort-by #(get-in % [:columns :value]) positive))
      (sort-by #(get-in % [:columns :value]) negative))))

(defn append-daydata [day transactions bar-width drawing-height svg-container yscale]
  (let [x (* (- day 1) bar-width)]
    (loop [the-transactions (bar-chart-sort transactions)
           positive-offset 0
           negativ-offset 0]
      (when-not (empty? the-transactions)
        (let [{:keys [columns]} (first the-transactions)
              the-val (yscale (:value columns))
              y (if (< the-val 0)
                  (+ (/ drawing-height 2) negativ-offset)
                  (- (/ drawing-height 2) positive-offset the-val))
              group-container (.append svg-container "g")]
          (-> (.append group-container "rect")
              (.attr "x" x)
              (.attr "y" y)
              (.attr "style" "fill:rgb(0,0,255);stroke-width:1;stroke:rgb(0,0,0)")
              (.attr "data-val" columns)
              (.attr "width" bar-width)
              (.attr "height" (Math/abs the-val)))
          (if (< the-val 0)
            (recur (rest the-transactions) positive-offset (+ negativ-offset (Math/abs the-val)))
            (recur (rest the-transactions) (+ positive-offset the-val) negativ-offset))
          )))))

(defn with-missing-days-as-empty [days]
  (loop [all-days (range 1 32)
         result days]
    (if (empty? all-days)
      result
      (if (nil? (get result (first all-days)))
        (recur (rest all-days) (assoc result (first all-days) []))
        (recur (rest all-days) result)))))

(defn bar-chart [raw-data]
  (let [{:keys [csv-data meta-data]} (edn/read-string raw-data)
        {:keys [max-withdrawal-per-day max-receival-per-day]} meta-data
        bar-width 40
        chart-width (* 31 bar-width)
        top-bottom-space 50
        drawing-height 500
        chart-height (+ drawing-height (* 2 top-bottom-space))
        yscale (d3YScale (max max-receival-per-day max-withdrawal-per-day) drawing-height)]
    (doseq [[year months] csv-data]
      (let [year-container (-> (js/d3.select "#barchart")
                               (.append "div")
                               (.attr "width" "100%")
                               (.attr "height" "100%"))]
        (doseq [[month days] months]
          (let [month-container (-> (.append year-container "div") (.attr "width" "100%") (.attr "height" "100%"))
                _ (-> (.append month-container "h3") (.html (str year "/" month)))
                svg-container (append-svg-container month-container [chart-width chart-height])]
            (doseq [[day transactions] (with-missing-days-as-empty days)]
              (append-daydata day transactions bar-width chart-height svg-container yscale))))))))


