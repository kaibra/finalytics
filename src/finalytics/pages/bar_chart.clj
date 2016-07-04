(ns finalytics.pages.bar-chart
  (:require
    [compojure.core :as c]
    [finalytics.pages.page-frame :as html]))

(def bar-chart-page
  (c/GET "/bar-chart" []
    (html/page-frame-response
      [:div {:class "row"}
       [:div {:class "col-lg-12"}
        [:div {:class "row"}
         [:h3 "Chart: "]]

        [:div {:class "row"}
         [:div {:id "barchart"}]]
        ]])))
