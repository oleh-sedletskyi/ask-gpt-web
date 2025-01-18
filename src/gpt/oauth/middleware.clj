(ns gpt.oauth.middleware
  (:require [gpt.system :as-alias system]
            [clojure.tools.logging :as log])
  (:import (io.github.cdimascio.dotenv Dotenv)))

(defn get-google-keys [env]
  {:client-id (or (System/getenv "GOOGLE-CLIENT-ID")
                  (Dotenv/.get env "GOOGLE-CLIENT-ID"))
   :client-token (or (System/getenv "GOOGLE-CLIENT-TOKEN")
                     (Dotenv/.get env "GOOGLE-CLIENT-TOKEN"))})

(defn oauth-config [{::system/keys [env]}]
  (log/info "Reading google keys for OAuth")
  {:google
   {:authorize-uri    "https://accounts.google.com/o/oauth2/auth"
    :access-token-uri "https://www.googleapis.com/oauth2/v4/token"
    :client-id        (:client-id (get-google-keys env))
    :client-secret    (:client-token (get-google-keys env))
    :scopes           ["email"]
    :launch-uri       "/login-with-google"
    :redirect-uri     "/oauth/google/callback"
    :landing-uri      "/oauth/google/done"}})
