(ns clojureflare.core)

(defn route [method path fn] {:method method :path path :handler fn})

(defn make-response [resp]
  (js/Response. (:body resp) {"status" (get-in resp [:params :status])}))

(defn assemble-handlers [routes]
  (into {}
        (map #(vector (:path %) %) routes)))

(defmulti routefn type)
(defmethod routefn String [resp] (make-response {:body resp :params {:status 200}}))
(defmethod routefn PersistentArrayMap [resp] (make-response resp))
(defmethod routefn :default [resp] (make-response (apply resp [])))


(defn handleRequest [req routes]
  (.log js/console (:path req))
  (let [r (assemble-handlers routes)]
    (if (contains? r (:path req))
      (routefn
        (get r (:path req)))
      (make-response {:params {:status 404} :body "Not Found"}))))
 

(defn extract-path [url]
  (.-pathname (new js/URL url)))

(defn convert-request [req]
  {:path (extract-path (.-url req))
   })

(defn worker [& routes] (js/addEventListener "fetch" 
  #(.respondWith % (handleRequest (convert-request (.-request %)) routes))))
