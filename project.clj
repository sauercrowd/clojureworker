(defproject com.github.sauercrowd/clojureflare "0.0.1"
  :description "Clojurescript library for Cloudflare Workers"
  :url "https://github.com/sauercrowd/clojureflare"
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-codox "0.10.7"]]
  :codox {:language :clojurescript}
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/clojurescript "1.10.339"]]
  :source-paths ["src"]
  :test-paths ["test"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
    :test-commands {"unit-tests" ["node" "target/tests.js"]}
    :builds {:tests
             {:source-paths ["src" "test"]
              :notify-command ["node" "target/tests.js"]
              :compiler {:output-to "target/tests.js"
                         :optimizations :none
                         :target :nodejs
                         :main clojureflare.core-test
                         }}
              :production
               {:source-paths ["src"]
                :compiler {:output-to "target/output.js"
                           :optimizations :advanced}}}})
