(ns finalytics.chart.utils
  (:require cljsjs.d3))

(defn append-svg-container [container {:keys [chart-width chart-height]}]
  (-> container
      (.append "svg")
      (.attr "width" "100%")
      (.attr "height" "100%")
      (.attr "viewBox" (str "0 0 " chart-width " " chart-height))))

(defn with-missing-days-as-empty [days]
  (loop [all-days (range 1 32)
         result days]
    (if (empty? all-days)
      result
      (if (nil? (get result (first all-days)))
        (recur (rest all-days) (assoc result (first all-days) []))
        (recur (rest all-days) result)))))

(defn d3YScale [max-value drawing-height]
  (let [half-drawing-height (/ drawing-height 2)]
    (-> (js/d3.scale.linear)
        (.domain (array (- max-value) max-value))
        (.range (array (- half-drawing-height) half-drawing-height)))))

(defn append-div [c]
  (-> (.append c "div") (.attr "width" "100%") (.attr "height" "100%")))

(defn render-chart [container csv-data render-chart-fn]
  (doseq [[year months] (reverse (sort csv-data))]
    (let [year-container (append-div container)]
      (doseq [[month days] (reverse (sort months))]
        (let [month-container (append-div year-container)]
          (-> (.append month-container "h3") (.html (str year "/" month)))
          (render-chart-fn
            month-container
            {:year  year
             :month month
             :days  days}))))))

