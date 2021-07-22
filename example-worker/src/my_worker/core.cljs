(ns my-worker.core
  (:require [clojureworker :as clfl]))

;; use simulate-worker to get a response returned by the function without any
;; browser dependencies

(clfl/simulate-worker
 {:method "GET" :path "/test" :headers {} :body nil}
 (clfl/route "GET" "/test" (fn [req]
                             (js/Promise. (fn [resolv cancel]
                                            (resolv {:body "promise resolved"})))))
  (clfl/route "GET" "/api/string-as-html" "cool response")
  (clfl/route "GET" "/api/map-as-json" {:hello 1})
  (clfl/route "POST" "/api/ping" #(identity {:body (:body %)
                                                :headers {}
                                                :status 200})))


;; use worker to connect the routes to the worker environment
(clfl/worker
 (clfl/route "GET" "/test" (fn [req]
                             (js/Promise. (fn [resolv cancel]
                                            (resolv {:body "promise resolved"})))))
  (clfl/route "GET" "/api/string-as-html" "cool response")
  (clfl/route "GET" "/api/map-as-json" {:hello 1})
  (clfl/route "POST" "/api/ping" #(identity {:body (:body %)
                                                :headers {}
                                                :status 200})))
