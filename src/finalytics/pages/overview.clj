(ns finalytics.pages.overview
  (:require [finalytics.csv.csv-loader :refer [csv-data]]))

(defn csv-data-stats []
  [:div
   "Nr. transactions: " (count csv-data)])

(defn page-content []
  [:div {:class "container"}
   [:div {:class "row"}
    [:div {:class "col-lg-6"}
     [:h1 "Finalytics"]
     (csv-data-stats)]]])
