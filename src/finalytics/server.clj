(ns finalytics.server
  (:require
    [kaibra.ms-httpkit :as httpk]
    [compojure.route :as croute]

    [finalytics.pages.bar-chart :as barchartpage]
    [finalytics.pages.csv-config :as csvconfigpage]
    [finalytics.pages.welcome :as welcome]
    [mount.core :refer [defstate]]))

(defstate server
          :start (httpk/start-server
                   (croute/resources "/")
                   barchartpage/bar-chart-page
                   csvconfigpage/get-csv-config-page
                   csvconfigpage/post-csv-config-page
                   welcome/welcome-page
                   )

          :stop (httpk/stop-server server))
