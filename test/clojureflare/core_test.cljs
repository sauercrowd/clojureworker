(ns clojureflare.core-test
  (:require [cljs.test]
            [clojureflare.core]))

                                        ; validate route schema
(cljs.test/deftest simple-route
  (let [r (clojureflare.core/route "GET" "/v1" "hello world")]
    (cljs.test/is (= r {:method "GET" :path "/v1" :handler "hello world"}))))

                                        ; check if URL can be extracted
(cljs.test/deftest extract-url
  (let [ep (clojureflare.core/extract-path (js/URL. "http://localhost/api/v1/test"))]
    (cljs.test/is (= ep "/api/v1/test"))))

                                        ; convert request from JS object
(cljs.test/deftest convert-request
  (let [cr (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test"})]
    (cljs.test/is (= cr {:path "/api/v1/test"}))))


(cljs.test/run-tests)
