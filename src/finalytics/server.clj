(ns finalytics.server
  (:require
    [kaibra.ms-httpkit :as httpk]
    [compojure.core :as c]
    [hiccup.page :as hic]
    [compojure.route :as croute]
    [mount.core :refer [defstate]]))

(def first-example-route
  (c/GET "/example1" []
    {:status 200
     :body   (hic/html5
               [:head (hic/include-js "js/main.js")
                [:meta {:http-equiv "content-type"
                        :content    "text/html; charset=UTF8"}]]
               [:body
                [:h1 "H IIC  CHAJB"]]
               )}))

(defstate server
          :start (httpk/start-server
                   (croute/resources "/")
                   first-example-route)
          :stop (httpk/stop-server server))