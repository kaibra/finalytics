(ns finalytics.html
  (:require [hiccup.page :as hic]
            [finalytics.csv.csv-loader :as loader]
            [clojure.data.json :as json]))

(defn page-frame [content]
  (hic/html5
    [:head
     (hic/include-js "js/jquery-2.2.3.min.js")
     (hic/include-js "js/bootstrap.min.js")
     (hic/include-css "css/bootstrap.min.css")
     (hic/include-css "css/bootstrap-theme.min.css")
     [:meta {:http-equiv "content-type"
             :content    "text/html; charset=UTF8"}]]
    [:body
     content
     (hic/include-js "js/main.js")
     [:script (str "var csvdata = " (json/write-str loader/csv-data))]]))

(defn html-response [content]
  {:status 200
   :body   (page-frame content)})
