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
  (let [cr (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET"})]
    (cljs.test/is (= cr {:path "/api/v1/test" :method "GET"}))))


                                        ; match an entire request to it's route
(cljs.test/deftest test-handler-match
  (let [req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET"})
        routes [(clojureflare.core/route "GET" "/api/v1/test" "hello-world")]
        expectation {:body "hello-world" :params {:status 200}}]
    (cljs.test/is (= expectation (clojureflare.core/handleRequest req routes)))))

                                        ; check if a request doesn't match any route
(cljs.test/deftest test-handler-404
  (let [req (clojureflare.core/convert-request #js {:url "http://localhost/garbled" :method "GET"})
        routes [(clojureflare.core/route "GET" "/api/v1/test" "hello")]
        expectation {:body "Not Found" :params {:status 404}}]
    (cljs.test/is (= expectation (clojureflare.core/handleRequest req routes)))))

                                        ; test if a map gets converted into JSON
(cljs.test/deftest test-json-route
  (let [req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET"})
        routes [(clojureflare.core/route "GET" "/api/v1/test" {:userid 1 :score 5})]
        expectation {:body (.stringify js/JSON (clj->js {:userid 1 :score 5}))
                     :params {:status 200 :headers {"Content-Type" "application/json"}}}]
    (cljs.test/is (= expectation (clojureflare.core/handleRequest req routes)))))

                                        ; test if a function route
(cljs.test/deftest test-fn-route
  (let [req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET"})
        routes [(clojureflare.core/route "GET" "/api/v1/test" #(identity {:body "nice function" :params {:status 200}}))]
        expectation {:body "nice function" :params {:status 200}}]
    (cljs.test/is (= expectation (clojureflare.core/handleRequest req routes)))))

;; test setup
(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "All tests succeeded!")
    (println "A test failed")))

(cljs.test/run-tests)
