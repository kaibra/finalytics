(ns finalytics.charts.barchart
  (:require cljsjs.d3
            [cljs.reader :as edn]))

(defn d3YScale [max-value drawing-height]
  (let [half-drawing-height (/ drawing-height 2)]
    (-> (js/d3.scale.linear)
        (.domain (array (- max-value) max-value))
        (.range (array (- half-drawing-height) half-drawing-height)))))

(defn append-svg-container [container [width height]]
  (-> container
      (.append "svg")
      (.attr "width" "100%")
      (.attr "height" "100%")
      (.attr "viewBox" (str "0 0 " width " " height))))

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
      (.attr "text-anchor"  "middle")
      (.attr "alignment-baseline"  "central")
      (.attr "style" (str "font-size:" (* axis-height (/ 4 5)) "px"))
      (.html day)
      )

  )

(defn append-daydata [day transactions bar-width drawing-height svg-container yscale axis-height]
  (let [x (* (- day 1) bar-width)
        axis-pos (/ drawing-height 2)]
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

(defn append-x-axis [svg-container chart-width chart-height axis-height]
  (-> (.append svg-container "rect")
      (.attr "x" 0)
      (.attr "y" (- (/ chart-height 2) (/ axis-height 2)))
      (.attr "width" chart-width)
      (.attr "height" axis-height)
      (.attr "fill-opacity" 0)
      (.attr "style" "stroke-width:1;stroke:rgb(0,0,0)")
      )
  )

(defn bar-chart [container csv-data {:keys [max-withdrawal-per-day max-receival-per-day]}]
  (let [drawing-height 500
        bar-width (/ drawing-height 10)
        chart-width (* 31 bar-width)
        top-bottom-space 50
        axis-height (/ bar-width 3)
        chart-height (+ drawing-height (* 2 top-bottom-space) axis-height)
        yscale (d3YScale (max max-receival-per-day max-withdrawal-per-day) drawing-height)]
    (doseq [[year months] (reverse (sort csv-data))]
      (let [year-container (-> container
                               (.append "div")
                               (.attr "width" "100%")
                               (.attr "height" "100%"))]
        (doseq [[month days] (reverse (sort months))]
          (let [month-container (-> (.append year-container "div") (.attr "width" "100%") (.attr "height" "100%"))
                _ (-> (.append month-container "h3") (.html (str year "/" month)))
                svg-container (append-svg-container month-container [chart-width chart-height])]
            (append-x-axis svg-container chart-width chart-height axis-height)
            (doseq [[day transactions] (with-missing-days-as-empty days)]
              (append-daydata day transactions bar-width chart-height svg-container yscale axis-height))))))))


