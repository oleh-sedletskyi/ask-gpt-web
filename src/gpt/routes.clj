(ns gpt.routes
  (:require
   [clojure.tools.logging :as log]
   [gpt.static.routes :as static-routes]
   [gpt.oauth.routes :as oauth-routes]
   [ring.middleware.oauth2 :refer [wrap-oauth2]]
   [muuntaja.middleware :as muuntaja]
   ;; [ring.util.http-response :as http]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   ;; [reitit.ring.middleware.dev :as dev] ;; debug middleware
   [tick.core :as t]
   [gpt.ask.routes :as gpt-routes]
   [gpt.oauth.middleware :as oauth-middleware]
   [gpt.system :as-alias system]
   [hiccup2.core :as h]
   [reitit.ring :as reitit-ring]
   [clojure.java.io :as io]
   [ring.util.response :as response]))

(defn serve-pdf-file
  "Serves a PDF file using File instead of resource"
  [_request]
  (let [filename "book.pdf"
        file (io/file "resources" filename)]
    (if (.exists file)
      (-> (response/response (io/input-stream file))
          (response/content-type "application/pdf")
          (response/header "Content-Disposition"
                           (str "attachment; filename=\"" filename "\"")))
      (response/not-found (str "PDF file not found: " filename)))))

(defn routes
  [system]
  [""
   (static-routes/routes system)
   (oauth-routes/routes system)
   (gpt-routes/routes system)
   ["/" {:get {:handler (fn [request]
                          {:status 200
                           :headers {"Content-Type" "text/html"}
                           :body
                           (str
                            (h/html
                             [:body
                              #_[:a {:href "/download-pdf"} "Book"]
                              #_[:br]
                              (when (get-in request [:flash :layout/message])
                                [:h3 {:style {:color "green"}}
                                 (get-in request [:flash :layout/message])])
                              [:a {:href "/login-with-google"} "Login with Google"]
                              [:br]
                              [:a {:href "/gpt"} "Ask GPT"]
                              [:br]
                              [:a {:href "/logout"} "Logout"]]))})}}]
   #_["/download-pdf" {:get {:handler serve-pdf-file}}]])

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
   (let [handler (-> (reitit.ring/ring-handler
                      (reitit.ring/router
                       (routes system)
                       ;; debug middleware
                       #_{:reitit.middleware/transform dev/print-request-diffs})
                      #'not-found-handler)
                     #_(muuntaja/wrap-format)
                     (wrap-oauth2 (oauth-middleware/oauth-config system))
                     (wrap-defaults (-> site-defaults
                                        ;; set :lax so that google can set the cookie
                                        (assoc-in [:session :cookie-attrs :same-site]
                                                  :lax))))]
     (fn root-handler [request]
       (log/info (str (t/now) " " (:request-method request) " - " (:uri request)))
       (handler request)))))

(comment
  (routes user/system)

  (require '[clojure.repl.deps :as deps])
  (deps/sync-deps)
;;
  )
