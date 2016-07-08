(ns finalytics.pages.page-frame
  (:require [hiccup.page :as hic]
            [finalytics.csv.csv-loader :as loader]))

(def responsive-menu-toggle-button
  [:button {:type          "button"
            :class         "navbar-toggle collapsed"
            :data-toggle   "collapse"
            :data-target   "#bs-example-navbar-collapse-1"
            :aria-expanded "false"}
   [:span {:class "sr-only"} "Toggle navigation"]
   [:span {:class "icon-bar"}]
   [:span {:class "icon-bar"}]
   [:span {:class "icon-bar"}]])

(def transaction-charts
  [:ul {:class "dropdown-menu"}
   [:li [:a {:href "barchart"} "Simple Bar Chart"]]
   [:li [:a {:href "waterfall"} "Waterfall Chart"]]])

(defn dropdown-anchor [atxt]
  [:a {:href          "#"
       :class         "dropdown-toggle"
       :data-toggle   "dropdown"
       :role          "button"
       :aria-haspopup "true"
       :aria-expanded "false"} atxt [:span {:class "caret"}]])

(def nav-bar
  [:nav {:class "navbar navbar-default"}
   [:div {:class "container-fluid"}
    [:div {:class "navbar-header"}
     responsive-menu-toggle-button
     [:a {:class "navbar-brand" :href "/"} "Finalytics"]]

    [:div {:class "collapse navbar-collapse" :id "bs-example-navbar-collapse-1"}
     [:ul {:class "nav navbar-nav"}

      [:li
       [:a {:href "csv-config"} "Input Data"
        [:span {:class "sr-only"} "(current)"]]]

      [:li {:class "dropdown"}
       (dropdown-anchor "Transaction Charts")
       transaction-charts]

      ]]]])

(defn page-frame [content {:keys [error]}]
  (hic/html5
    [:head
     (hic/include-js "js/jquery-2.2.3.min.js")
     (hic/include-js "js/bootstrap.min.js")
     (hic/include-css "css/bootstrap.min.css")
     (hic/include-css "css/bootstrap-theme.min.css")
     [:meta {:http-equiv "content-type"
             :content    "text/html; charset=UTF8"}]]
    [:body
     nav-bar
     [:div {:class "container"}
      (when error
        [:div {:class "row"}
         [:div {:class "alert alert-danger"}
          [:strong "Error: " error]]])
      content]
     [:script (str "var csvdata = '" (.replaceAll (str loader/csv-data) "'" "") "'")]
     (hic/include-js "js/main.js")
     ]))

(defn page-frame-response [content & stuff]
  {:status 200
   :body   (page-frame content stuff)})
