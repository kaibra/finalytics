(ns finalytics.core)

(enable-console-print!)

(defn init []
  (println "Hello from ClojureScript!"))



(set! (.-onload js/window) init)




