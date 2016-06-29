(ns finalytics.pages.overview
  (:require
    [compojure.core :as c]
    [finalytics.html :as html]
    [mount.core :as mount]
    [finalytics.csv.csv-loader :as csvload]))

(defn csv-data-stats []
  (let [meta-info (:meta-data csvload/csv-data)]
    [:div
     "Nr. transactions: " (:nr-transactions meta-info) [:br]
     "Max. val per day: " (:max-per-day meta-info) [:br]
     "Max. abs per day: " (:max-abs-per-day meta-info) [:br]
     "Max-withdrawal-per-day: " (:max-withdrawal-per-day meta-info) [:br]
     "Max-receival-per-day: " (:max-receival-per-day meta-info) [:br]
     ]))

(defn overview-body [& {:keys [error]}]
  (html/html-response
    [:div {:class "container"}
     [:div {:class "row"}
      [:h1 "Finalytics"]]

     (when error
       [:div {:class "row"}
        [:div {:class "alert alert-danger"}
         [:strong "Error: " error]]])

     [:div {:class "row"}
      [:form {:method "post" :action "/overview"}
       [:div {:class "col-lg-6 form-group"}
        [:div {:class "row"}
         [:h3 "Config:"]]
        [:div {:class "row"}
         [:label {:for "csv-folder"} "csv-folder"]
         [:input {:type "text" :class "form-control" :value @csvload/data-folder :name "csv-folder" :id "csv-folder"}]]
        [:div {:class "row"}
         [:label {:for "spec-folder"} "spec-folder"]
         [:input {:type "text" :class "form-control" :value @csvload/spec-file :name "spec-folder" :id "spec-folder"}]]
        [:div {:class "row"}
         [:br]
         [:button {:type "submit" :class "btn btn-default"} "Load folders"]]]]

      [:div {:class "col-lg-6"}
       [:h3 "CSV-stats"]
       (csv-data-stats)]]

     [:div {:class "row"}
      [:div {:class "col-lg-12"}
       [:div {:class "row"}
        [:h3 "Chart: "]]

       [:div {:class "row"}
        [:div {:id "barchart"}]]
       ]]
     ]))

(def overview-route "/overview")

(def get-overview-page
  (c/GET overview-route []
    (overview-body)))

(defn invalid-folders? [csv-folder spec-folder]
  (or
    (or (nil? csv-folder) (empty? csv-folder))
    (or (nil? spec-folder) (empty? spec-folder))))

(def post-overview-page
  (c/POST overview-route [:as {:keys [params]}]
    (let [{:keys [csv-folder spec-folder]} (clojure.walk/keywordize-keys params)]
      (if-not (invalid-folders? csv-folder spec-folder)
        (do
          (csvload/update-and-reload-csv-data csv-folder spec-folder)
          (overview-body))
        (overview-body :error "Folders specified are not valid!")))))
