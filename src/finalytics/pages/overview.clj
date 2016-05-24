(ns finalytics.pages.overview
  (:require [finalytics.csv.parsing :as pars]
            [finalytics.csv.csv-loader :refer [csv-data]])
  )


(defn page-content []
  [:div {:class "container"}
   [:div {:class "row"}
    [:div {:class "col-lg-6"}
     [:h1 "Finalytics"]
     csv-data


     ]]])

