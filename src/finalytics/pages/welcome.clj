(ns finalytics.pages.welcome
  (:require [compojure.core :as c]
            [finalytics.pages.page-frame :as html]))


(def welcome-page
  (c/GET "/" []
         (html/page-frame-response
           [:div {:class "row"}
            [:h1 "Salut!"]
            ]
           )))
