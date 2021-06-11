(ns clojureflare.core-test
  (:require [cljs.test]
            [clojureflare.core]
            [cljs.core.async :refer [go chan <! >!]]))

(defn no-body-fn [] (.resolve js/Promise nil))

                                        ; validate route schema
(cljs.test/deftest simple-route
  (let [r (clojureflare.core/route "GET" "/v1" "hello world")]
    (cljs.test/is (= r {:method "GET" :path "/v1" :handler "hello world"}))))

                                        ; check if URL can be extracted
(cljs.test/deftest extract-url
  (let [ep (clojureflare.core/extract-path (js/URL. "http://localhost/api/v1/test"))]
    (cljs.test/is (= ep "/api/v1/test"))))

                                        ; convert request from JS object
(defn extract-relevant [m] (select-keys m [:path :headers :method]))

(defn check-req [routes chan expected done]
  (go
    (let [cr (<! chan)]
      (cljs.test/is (= (clojureflare.core/handle-request cr routes) expected))
      (done))))
 
(cljs.test/deftest convert-request
  (cljs.test/async done
    (go
      (let [req-ch (chan)
            _ (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET" :text no-body-fn} req-ch)
            cr (<! req-ch)]
        (cljs.test/is (= cr {:path "/api/v1/test" :method "GET" :headers nil :body nil}))
        (done)))))

                                        ; match an entire request to it's route
(cljs.test/deftest test-handler-match
  (cljs.test/async done
    (let [req-ch (chan)
          _ (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" "hello-world")]
          expectation {:body "hello-world" :status 200 :headers {"Content-Type" "text/html"}}] 
      (check-req routes req-ch expectation done))))

                                        ; make sure method mismatch results in 404
(cljs.test/deftest test-handler-method-mismatch
  (cljs.test/async done    
    (let [req-ch (chan)
          req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "POST" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" "hello-world")]
          expectation {:body "Not Found" :status 404}
          _ (clojureflare.core/handle-request req routes)]
      (check-req routes req-ch expectation done))))

                                        ; check if a request doesn't match any route
(cljs.test/deftest test-handler-404
  (cljs.test/async done
    (let [req-ch (chan)
          req (clojureflare.core/convert-request #js {:url "http://localhost/garbled" :method "GET" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" "hello")]
          expectation {:body "Not Found" :status 404}
          _ (clojureflare.core/handle-request req routes)]
      (check-req routes req-ch expectation done))))

                                        ; test if a map gets converted into JSON
(cljs.test/deftest test-json-route
  (cljs.test/async done    
    (let [req-ch (chan)
          req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" {:userid 1 :score 5})]
          expectation {:body (.stringify js/JSON (clj->js {:userid 1 :score 5}))
                       :status 200 :headers {"Content-Type" "application/json"}}
          _ (clojureflare.core/handle-request req routes)]
      (check-req routes req-ch expectation done))))

                                        ; test if a function route
(cljs.test/deftest test-fn-route
  (cljs.test/async done
    (let [req-ch (chan)
          req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" #(identity {:body "nice function" :status 200}))]
          expectation {:body "nice function" :status 200}
          _ (clojureflare.core/handle-request req routes)]
      (check-req routes req-ch expectation done))))

                                        ; test if a function route with an arg
(cljs.test/deftest test-fn-route-with-arg
  (cljs.test/async done
    (let [req-ch (chan)
          req (clojureflare.core/convert-request #js {:url "http://localhost/api/v1/test" :method "GET" :text no-body-fn} req-ch)
          routes [(clojureflare.core/route "GET" "/api/v1/test" #(identity
                                                                  {:body (str "nice function " (:path %)) :status 200}))]
          expectation {:body "nice function /api/v1/test" :status 200}
          _ (clojureflare.core/handle-request req routes)]
      (check-req routes req-ch expectation done))))


;; test setup
(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "All tests succeeded!")
    (println "A test failed")))

(cljs.test/run-tests)
