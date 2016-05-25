(ns finalytics.server
  (:require
    [kaibra.ms-httpkit :as httpk]
    [compojure.core :as c]
    [hiccup.page :as hic]
    [compojure.route :as croute]
    [finalytics.pages.overview :as overview]
    [mount.core :refer [defstate]]))

(defn page-route-for [route content]
  (c/GET route []
    {:status 200
     :body   (hic/html5
               [:head
                (hic/include-js "js/jquery-2.2.3.min.js")
                (hic/include-js "js/bootstrap.min.js")
                (hic/include-css "css/bootstrap.min.css")
                (hic/include-css "css/bootstrap-theme.min.css")
                [:meta {:http-equiv "content-type"
                        :content    "text/html; charset=UTF8"}]]
               [:body
                (content)
                (hic/include-js "js/main.js")])}))

(defstate server
          :start (httpk/start-server
                   (croute/resources "/")
                   (page-route-for "/" overview/page-content))
          :stop (httpk/stop-server server))
