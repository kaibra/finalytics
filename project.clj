(defproject finalytics "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [kaibra/mount-ms "0.0.3"]
                 [kaibra/ms-httpkit "0.0.2"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "1.8.51"
                  :exclusions [org.apache.ant/ant]]

                 [cljsjs/d3 "3.5.16-0"]

                 [org.clojure/tools.logging "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.slf4j/slf4j-api "1.7.16"]
                 [ch.qos.logback/logback-core "1.1.5"]
                 [ch.qos.logback/logback-classic "1.1.5"]
                 ]
  :plugins [[lein-cljsbuild "1.1.3"]]
  :source-paths ["src" "src-clj"]
  :main finalytics.core
  :profiles {:dev {:dependencies [[me.lomin/component-restart "0.1.0"]]
                   :main finalytics.testcore}}
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler     {:output-to     "resources/public/js/main.js"
                                       :optimizations :whitespace
                                       :pretty-print  false}}]})
