(ns finalytics.pages.overview
  (:require
    [compojure.core :as c]
    [finalytics.html :as html]
    [mount.core :as mount]
    [finalytics.csv.csv-loader :as csvload]))

(defn csv-data-stats []
  [:div
   "Nr. transactions: " (count csvload/csv-data)
   "efef" [:br]
   "efef" [:br]
   "efef" [:br]
   "efef" [:br]
   "efef" [:br]])

(defn overview-body []
  (html/html-response
    [:div {:class "container"}
     [:div {:class "row"}
      [:div {:class "col-lg-12"}
       [:h1 "Finalytics"]]]

     [:div {:class "row"}
      [:form {:method "post" :action "/overview"}
       [:div {:class "col-lg-6 form-group"}
        [:div {:class "row"}
         [:h3 "config:"]]
        [:div {:class "row"}
         [:label {:for "csv-folder"} "csv-folder"]
         [:input {:type "text" :class "form-control" :value @csvload/data-folder :name "csv-folder" :id "csv-folder"}]]
        [:div {:class "row"}
         [:label {:for "spec-folder"} "spec-folder"]
         [:input {:type "text" :class "form-control" :value @csvload/spec-folder :name "spec-folder" :id "spec-folder"}]]
        [:div {:class "row"}
         [:button {:type "submit" :class "btn btn-default"} "Load folders"]]]]

      [:div {:class "col-lg-6"}
       [:h3 "csv-stats:"]
       (csv-data-stats)]]]))

(def overview-route "/overview")

(def get-overview-page
  (c/GET overview-route []
    (overview-body)))

(defn update-and-reload-csv-data [csv-folder spec-folder]
  (reset! csvload/data-folder csv-folder)
  (reset! csvload/spec-folder spec-folder)
  (csvload/reload))


(def post-overview-page
  (c/POST overview-route [:as {:keys [params]}]
    (let [{:keys [csv-folder spec-folder]} (clojure.walk/keywordize-keys params)]
      (println csv-folder spec-folder params)
      (if-not (or
                (nil? csv-folder)
                (nil? spec-folder))
        (do
          (update-and-reload-csv-data csv-folder spec-folder)
          (overview-body))
        (html/bad-request "Folders specified are not valid!")))))

