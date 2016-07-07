(ns finalytics.pages.chart-frame
  (:require
    [compojure.core :as c]
    [finalytics.pages.page-frame :as html]))

(defn- chart-frame [id]
  (c/GET (str "/" id) []
    (html/page-frame-response
      [:div {:class "row"}
       [:div {:class "col-lg-12"}
        [:div {:class "row"}
         [:h3 "Chart: "]]

        [:div {:class "row"}
         [:div {:id id}]]
        ]])))

(def bar-chart-id "barchart")
(def waterfall-chart-id "waterfall")

(def bar-chart-route
  (chart-frame bar-chart-id))

(def waterfall-chart-route
  (chart-frame waterfall-chart-id))


