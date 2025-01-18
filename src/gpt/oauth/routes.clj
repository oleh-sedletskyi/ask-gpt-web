(ns gpt.oauth.routes
  (:require [gpt.middleware :as middleware]
            [ring.util.response :as response]
            [gpt.system :as-alias system]
            [gpt.layout :as layout]
            [cheshire.core :as json]
            [org.httpkit.client :as hk-client])
  (:import (io.github.cdimascio.dotenv Dotenv)))

(def userinfo-url "https://www.googleapis.com/oauth2/v1/userinfo")

(defn google-fetch-email [token]
  (->
   @(hk-client/get userinfo-url
                   {:headers
                    {"Content-Type" "application/json"}
                    :query-params {:access_token token}})
   :body
   (json/decode keyword)
   :email))

(defn routes
  [system]
  [""
   #_{:middleware (middleware/standard-html-route-middleware system)}
   ["/oauth/google/done"
    {:get
     {:handler (fn [req]
                 (let [token (get-in req [:oauth2/access-tokens :google :token])
                       email (google-fetch-email token)
                       next-session (-> (assoc (:session req) :identity email)
                                        (with-meta {:recreate true})
                                        (dissoc :ring.middleware.oauth2/access-tokens))]
                   (-> (response/redirect "/gpt")
                       (assoc :session next-session)
                       (layout/flash-message (str "OAuth login successful, welcome back " email)))))}}]
   ["/logout"
    {:get
     {:handler (fn [req]
                 (-> (response/redirect "/")
                     (assoc :session nil)
                     (layout/flash-message "You have been logged out")))}}]])

(comment
  (let [{::system/keys [env]} user/system]

    #_(get-google-keys env))
  ;;
  )
