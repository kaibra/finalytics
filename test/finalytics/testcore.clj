(ns finalytics.testcore
  (:require
    [finalytics.core :as core]
    [com.stuartsierra.component :as c]
    [me.lomin.component-restart :as restart])
  (:gen-class))

(defrecord TestSystem []
  c/Lifecycle
  (start [self] (core/start) self)
  (stop [self] (core/stop) self))

(defn -main [& args]
  (println "Thats the test core")
  (let [system (c/start (->TestSystem))]
    (restart/watch (var -main) system)))
