(ns finalytics.html
  (:require [hiccup.page :as hic]))

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
     (hic/include-js "js/main.js")]))

(defn html-response [content]
  {:status 200
   :body   (page-frame content)})

(defn bad-request [message]
  {:status 400
   :body   (page-frame
             [:div {:class "container"}
              [:div {:class "row"}
               [:div {:class "col-lg-12"}
                [:div {:class "alert alert-danger"}
                 [:strong "Bad Request: " message]]]]])})
