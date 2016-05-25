(ns finalytics.overview
  (:require cljsjs.d3))

(enable-console-print!)

(println "Hello from ClojureScript! using d3 version: " js/d3.version)

(defn register-all-listeners []
  (println "Register all")
  #_(-> (js/d3.select "#reload-csv")
        (.on "click" (fn []

                       (js/console.log "FOOO")))))

(register-all-listeners)
