(ns finalytics.charts.barchart
  (:require cljsjs.d3))

(defn max-val [csv-data]
  (let [items (map (fn [d] (Math/abs d/columns.value)) csv-data)]
    (apply max items)))

(defn d3YScale [max-value half-height]
  (-> (js/d3.scale.linear)
      (.domain (array (- max-value) max-value))
      (.range (array (- half-height) half-height))))

(defn add-svg-background [svg-container]
  (-> svg-container
      (.append "rect")
      (.attr "fill" "blue")
      (.attr "width" "100%")
      (.attr "height" "100%")))

(defn append-svg-container [selector [width height]]
  (doto (-> (js/d3.select selector)
            (.append "svg")
            (.attr "viewBox" (str "0 0 " width " " height)))
    (add-svg-background)))

(defn bind-data [svg-container csv-data]
  (-> (.selectAll svg-container "div")
      (.data csv-data)))

(defn on-enter [svg-container append-fn & args]
  (-> (.enter svg-container)
      (append-fn args)))

(defn bar-chart-y-val [half-height y-scale [_ y-start] d i]
  (+ y-start
     (- half-height (Math/max 0 (y-scale d/columns.value)))))

(defn bar-chart-x-val [single-width [x-start _] d i]
  (+ x-start (* i single-width)))

(defn bar-chart-text-transformation [half-height single-width y-scale font-size start-pos d i]
  (let [the-val d/columns.value
        the-val-scaled (y-scale the-val)
        x-val (+ (bar-chart-x-val single-width start-pos d i) (* (- single-width font-size) (/ 4 5)))
        y-val (bar-chart-y-val half-height y-scale start-pos d i)
        y-val (if (< the-val 0)
                (- y-val the-val-scaled)
                y-val)]
    (str "translate(" x-val "," y-val ")rotate(90)")))

(defn transaction-as-single-line [d]
  (loop [k (cljs.core/js-keys d/columns)
         result ""]
    (let [current-field (first k)]
      (if (empty? k)
        result
        (recur (rest k)
               (str result (str " "current-field ": " (aget d/columns current-field))))))))

(defn append-barchart-rect [single-width half-height yscale start-pos total-height svg-container]
  (let [group-container (.append svg-container "g")
        font-size (* single-width (/ 2 3))]
    (-> (.append group-container "rect")
        (.attr "x" (partial bar-chart-x-val single-width start-pos))
        (.attr "y" (partial bar-chart-y-val half-height yscale start-pos))
        (.attr "width" single-width)
        (.attr "height" (fn [d _] (Math/abs (yscale d/columns.value)))))

    (-> (.append group-container "text")
        (.attr "fill" "white")
        (.attr "font-size" font-size)
        (.attr "transform" (partial bar-chart-text-transformation half-height single-width yscale font-size start-pos))
        (.html (fn [d _] d/columns.value)))

    (-> (.append svg-container "text")
        (.attr "x" 5)
        (.attr "y" 20)
        (.attr "id" (fn [_ i] (str "bartext" i)))
        (.attr "fill" "yellow")
        (.attr "visibility" "hidden")
        (.html (fn [d i]
                 (str "Transaction " i ":"
                      (transaction-as-single-line d)))))

    #_(-> (.append group-container "rect")
        (.attr "id" (fn [_ i] (str "bar" i)))
        (.attr "fill-opacity" "0")
        (.attr "x" (partial bar-chart-x-val single-width start-pos))
        (.attr "y" 0)
        (.attr "width" single-width)
        (.attr "height" total-height)
        (.on "mouseover" (fn [_ i]
                           (-> (js/d3.select (str "#bartext" i))
                               (.attr "visibility" "visible"))))
        (.on "mouseout" (fn [_ i]
                          (-> (js/d3.select (str "#bartext" i))
                              (.attr "visibility" "hidden")))))))

(defn draw-data [svg-container csv-data total-height & {:keys [data-dim start-pos]}]
  (let [[width height] data-dim
        single-width (int (/ width (count csv-data)))
        half-height (int (/ height 2))
        yscale (d3YScale (max-val csv-data) half-height)]
    (-> (bind-data svg-container csv-data)
        (on-enter (partial append-barchart-rect single-width half-height yscale start-pos total-height)))))

(defn bar-chart [csv-data]
  (let [svg-width 1000
        svg-height 400
        top-bottom-space 100
        svg-container (append-svg-container "#barchart" [svg-width svg-height])]
    (draw-data svg-container csv-data svg-height
               :data-dim [svg-width (- svg-height top-bottom-space)]
               :start-pos [0 (/ top-bottom-space 2)])))

