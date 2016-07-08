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

(defn insert-div [c]
  (-> (.insert c "div" ":first-child") (.attr "width" "100%") (.attr "height" "100%")))

(defn append-month-headline [month-container year month]
  (-> (.append month-container "h3")
      (.html (str year "/" month))))

(defn render-months [last-year-result year-container render-chart-fn year months]
  (loop [last-month-result last-year-result
         data (sort months)]
    (when-let [[month days] (first data)]
      (let [month-container (insert-div year-container)]
        (append-month-headline month-container year month)
        (-> (render-chart-fn
              last-month-result
              month-container
              {:year  year
               :month month
               :days  days})
            (recur (rest data)))))))

(defn render-chart [container csv-data render-chart-fn]
  (loop [last-result nil
         data (sort csv-data)]
    (when-let [[year months] (first data)]
      (let [year-container (insert-div container)]
        (-> (render-months last-result year-container render-chart-fn year months)
            (recur (rest data)))))))
