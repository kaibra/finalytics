(ns finalytics.pages.csv-config
  (:require [compojure.core :as c]
            [finalytics.csv.csv-loader :as csvload]
            [finalytics.pages.page-frame :as html]))

(defn csv-data-stats []
  (let [meta-info (:meta-data csvload/csv-data)]
    [:div
     "Nr. transactions: " (:nr-transactions meta-info) [:br]
     "Max. val per day: " (:max-per-day meta-info) [:br]
     "Max. abs per day: " (:max-abs-per-day meta-info) [:br]
     "Max-withdrawal-per-day: " (:max-withdrawal-per-day meta-info) [:br]
     "Max-receival-per-day: " (:max-receival-per-day meta-info) [:br]
     ]))

(defn csv-config-html []
  [:div {:class "row"}
   [:form {:method "post" :action "/csv-config"}
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
    (csv-data-stats)]])


(defn invalid-folders? [csv-folder spec-folder]
  (or
    (or (nil? csv-folder) (empty? csv-folder))
    (or (nil? spec-folder) (empty? spec-folder))))


(def get-csv-config-page
  (c/GET "/csv-config" []
    (html/page-frame-response
      (csv-config-html))))

(def post-csv-config-page
  (c/POST "/csv-config" [:as {:keys [params]}]
    (let [{:keys [csv-folder spec-folder]} (clojure.walk/keywordize-keys params)]
      (if-not (invalid-folders? csv-folder spec-folder)
        (do
          (csvload/update-and-reload-csv-data csv-folder spec-folder)
          (html/page-frame-response
            (csv-config-html)))
        (html/page-frame-response
          (csv-config-html)
          :error "Folders specified are not valid!")))))