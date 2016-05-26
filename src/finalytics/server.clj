(ns finalytics.server
  (:require
    [kaibra.ms-httpkit :as httpk]
    [hiccup.page :as hic]
    [compojure.route :as croute]

    [finalytics.pages.overview :as overview]
    [mount.core :refer [defstate]]))

(defstate server
          :start (httpk/start-server
                   (croute/resources "/")
                   overview/get-overview-page
                   overview/post-overview-page)

          :stop (httpk/stop-server server))
