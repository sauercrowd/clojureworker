(ns clojureflare.core)

(defn route [method path fn] {:method method :path path :handler fn})

(defn make-response [resp]
  (js/Response. (:body resp)
                {"status" (get-in resp [:params :status])
                 "headers" (get-in resp [:params :headers])}))

(defn get-key-from-req [req]
  (str (:method req) (:path req)))

(defn assemble-handlers [routes]
  (into {} (map
            (juxt get-key-from-req identity) routes)))


(defmulti routefn (fn [resp req] (type resp)))
(defmethod routefn js/String [resp _] {:body resp :params {:status 200}})

(defmethod routefn PersistentArrayMap [resp _]
  {:body (.stringify js/JSON (clj->js resp))
   :params {:status 200
            :headers { "Content-Type" "application/json"}}})

(defmethod routefn :default [resp req] (apply resp [req]))


(defn handleRequest [req routes]
  (let [rendered-routes (assemble-handlers routes)
        req-key (get-key-from-req req)]
    (if (contains? rendered-routes req-key)
      (routefn
        (:handler (get rendered-routes req-key)) req)
      {:params {:status 404} :body "Not Found"})))
 

(defn extract-path [url]
  (.-pathname (js/URL. url)))

(defn convert-request [req]
  {:path (extract-path (.-url req))
   :method (.-method req)
   :headers (.-headers req)
   :body #(.text req)})

(defn worker [& routes] (js/addEventListener "fetch" 
  #(.respondWith % (make-response (handleRequest (convert-request (.-request %)) routes)))))
