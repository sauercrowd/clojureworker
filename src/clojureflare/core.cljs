(ns clojureflare.core
  (:require [cljs.core.async :refer [go chan <! >!]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn route [method path handler] {:method method :path path :handler handler})

(defn ^:private make-response [resp]
  (js/Response. (:body resp)
                {"status" (get-in resp [:params :status])
                 "headers" (get-in resp [:params :headers])}))

(defn ^:private get-key-from-req [req]
  (str (:method req) (:path req)))

(defn ^:private assemble-handlers [routes]
  (into {} (map
            (juxt get-key-from-req identity) routes)))


(defmulti ^:private routefn (fn [resp req] (type resp)))

(defmethod ^:private routefn js/String [resp _] {:body resp :params {:status 200}})

(defmethod ^:private routefn PersistentArrayMap [resp _]
  {:body (.stringify js/JSON (clj->js resp))
   :params {:status 200
            :headers { "Content-Type" "application/json"}}})

(defmethod ^:private routefn :default [resp req] (apply resp [req]))


(defn ^:private handleRequest [req routes]
  (let [rendered-routes (assemble-handlers routes)
        req-key (get-key-from-req req)]
    (if (contains? rendered-routes req-key)
      (routefn
        (:handler (get rendered-routes req-key)) req)
      {:params {:status 404} :body "Not Found"})))
 

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
            (-> converted-req
                (handleRequest routes)
                (make-response)))))))))

(defn worker [& routes]
  (js/addEventListener "fetch"
                       #(worker-event-listener % routes)))
