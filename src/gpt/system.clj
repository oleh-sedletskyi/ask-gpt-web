(ns gpt.system
  (:require [gpt.routes :as routes]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.session.cookie :as session-cookie])
  (:import (io.github.cdimascio.dotenv Dotenv)
           (org.eclipse.jetty.server Server)))

(defn start-cookie-store
  []
  (session-cookie/cookie-store))

(defn start-server
  [{::keys [env] :as system}]
  (let [handler (if (= (Dotenv/.get env "ENVIRONMENT") "development")
                  (partial #'routes/root-handler system)
                  (routes/root-handler system))]
    (jetty/run-jetty
     handler
     {:port (Long/parseLong (Dotenv/.get env "PORT"))
      :join? false})))

(defn stop-server [^Server server]
  (.stop server))

(defn start-env
  []
  (Dotenv/load))

(defn start-system []
  (let [system-so-far {::env (start-env)}
        system-so-far (merge system-so-far {::cookie-store (start-cookie-store)})]
    (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system [system]
  (stop-server (::server system)))
