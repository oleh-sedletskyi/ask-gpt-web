(ns gpt.routes
  (:require
   [clojure.tools.logging :as log]
   [gpt.static.routes :as static-routes]
   [gpt.ask.routes :as gpt-routes]
   [hiccup2.core :as h]
   [reitit.ring :as reitit-ring]))

(defn routes
  [system]
  [""
   (static-routes/routes system)
   (gpt-routes/routes system)])

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (str (h/html [:html
                       [:body
                        [:h1 "Not found"]]]))})

(defn root-handler
  ([system request]
   ((root-handler system) request))
  ([system]
   (let [handler (reitit.ring/ring-handler
                  (reitit.ring/router
                   (routes system))
                  #'not-found-handler)]
     (fn root-handler [request]
       (log/info (str (:request-method request) " - " (:uri request)))
       (handler request)))))
