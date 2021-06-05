(ns my-worker.core
  (:require [clojureflare.core :as clfl]))

(clfl/worker
  (clfl/route "GET" "/v1/api/test" "cool response")
  (clfl/route "POST" "/v1/api/ping" #(identity {:body (:body %)
                                                :params {:headers {}
                                                         :status 700}})))
