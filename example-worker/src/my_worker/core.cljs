(ns my-worker.core
  (:require [clojureflare.core :as clfl]))

;; use simulate-worker to get a response returned by the function without any
;; browser dependencies

(clfl/simulate-worker
  {:method "GET" :path "/api/ping" :headers {} :body nil}
  (clfl/route "GET" "/api/test" "cool response")
  (clfl/route "GET" "/api/json" {:hello 1})
  (clfl/route "POST" "/api/ping" #(identity {:body (:body %)
                                                :headers {}
                                                :status 200})))

;; use worker to connect the routes to the worker environment
(clfl/worker
  (clfl/route "GET" "/api/test" "cool response")
  (clfl/route "GET" "/api/json" {:hello 1})
  (clfl/route "POST" "/api/ping" #(identity {:body (:body %)
                                                :headers {}
                                                :status 200})))
