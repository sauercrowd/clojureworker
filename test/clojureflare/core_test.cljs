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


(cljs.test/deftest test-handler-match
  (let [req #js {:url "http://localhost/api/v1/test"}
        routes [(clojureflare.core/route "GET" "/api/v1/test" "hello-world")]
        expectation {:body "hello-world" :params {"status" 200}}]
    (cljs.test/is (= expectation (clojureflare.core/handleRequest req routes)))))

(cljs.test/run-tests)
