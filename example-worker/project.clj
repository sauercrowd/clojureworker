(defproject example-worker "0.1.0-SNAPSHOT"
  :description "Example Clojure Cloudflare worker"
  :plugins [[lein-cljsbuild "1.1.8"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.339"]]
  :source-paths ["src"]
  :hooks [leiningen.cljsbuild]
  :resource-paths ["../target/clojureflare-0.1.0-SNAPSHOT.jar"]
  :cljsbuild {
    :builds {:production
               {:source-paths ["src"]
                :compiler {:output-to "target/worker.js"
                           :optimizations :advanced}}}})
