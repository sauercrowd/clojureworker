(ns my-worker.core
  (:require [clojureflare.core :as clfl]))

(clfl/worker
  (clfl/route "GET" "/v1/api/test" "cool respons"))
