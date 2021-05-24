(ns clojureflare.core-test
  (:require [cljs.test]
            [clojureflare.core]))

; simple route 
(cljs.test/deftest simple-route
  (let [r (clojureflare.core/route "GET" "/v1" "hello world")]
    (cljs.test/is (= r {:method "GET" :path "/v1" :handler "hello world"}))))




(cljs.test/run-tests)
