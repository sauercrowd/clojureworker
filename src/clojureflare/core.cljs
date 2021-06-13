(ns clojureflare.core
  (:require [cljs.core.async :refer [go chan <! >!]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn route
  "Create a new route with the given parameters:
  method: string, such as GET, POST, DELETE, ...
  path: string, e.g /my/api/v1/operation
  handler: either a
    string, which will be returned on a request with status code 200 and a text/html content-type
    map, which will be returned as a JSON with status code 200 and a application/json content-type
    function, which takes zero or one parameter. That function must return a map containing the keywords
      - :body (required, string)
      - :headers (map, optional, default {})
      - :status (integer, optional, default 200)
      If the function takes a parameter it will be the request made, using the same structure as above."
  [method path handler]
  {:method method :path path :handler handler})

(defn ^:private make-response [resp]
  (js/Response. (:body resp)
                (clj->js {"status" (get resp :status 200)
                          "headers" (get resp :headers {})})))

(defn ^:private get-key-from-req [req]
  (str (:method req) (:path req)))

(defn ^:private assemble-handlers [routes]
  (into {} (map
            (juxt get-key-from-req identity) routes)))


(defmulti ^:private routefn (fn [resp req] (type resp)))

(defmethod ^:private routefn js/String [resp _] {:body resp :status 200 :headers {"Content-Type" "text/html"}})

(defmethod ^:private routefn PersistentArrayMap [resp _]
  {:body (.stringify js/JSON (clj->js resp))
   :status 200
   :headers { "Content-Type" "application/json"}})

(defmethod ^:private routefn :default [resp req] (apply resp [req]))


(defn ^:private handle-request [req routes]
  (let [rendered-routes (assemble-handlers routes)
        req-key (get-key-from-req req)]
    (if (contains? rendered-routes req-key)
      (routefn
        (:handler (get rendered-routes req-key)) req)
      {:status 404 :body "Not Found"})))
 

(defn ^:private extract-path [url]
  (.-pathname (js/URL. url)))


(defn ^:private convert-request [req req-chan]
  (go
    (let [body (<p! (.text req))]
      (>! req-chan
          {:path (extract-path (.-url req))
           :method (.-method req)
           :headers (.-headers req)
           :body body}))))

(defn ^:private worker-event-listener [req routes]
  (.respondWith req
    (js/Promise. (fn [resolve reject]
      (go
        (let [req-chan (chan)
              _ (convert-request (.-request req) req-chan)
              converted-req (<! req-chan)]
          (resolve
            (make-response
              (let [resp (handle-request routes converted-req)]
                  (if (instance? js/Promise resp)
                    (<p! resp)
                    (identity resp)))))))))))

(defn simulate-worker
  "A function to simplify REPL development by simulating how a request would be routed
  and handled. The requests are maps with the keywords :status :headers :body :method, similiar as described in
  the route function"
  [req & routes]
  (handle-request req routes))

(defn worker
  "Takes all routes and registers the fetch event from the worker environment.
  Should only be called once per worker."
  [& routes]
  
  (js/addEventListener "fetch"
                       #(worker-event-listener % routes)))
