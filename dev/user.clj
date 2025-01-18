(ns user
  (:require [gpt.system :as system]))

(def system nil)

(defn start-system! []
  (if system
    (println "Already Started")
    (alter-var-root #'system (constantly (system/start-system)))))

(defn stop-system! []
  (when system
    (system/stop-system system)
    (alter-var-root #'system (constantly nil))))

(defn restart-system! []
  (stop-system!)
  (start-system!))

(defn server []
  (::system/server system))

(defn env
  []
  (::system/env system))

(defn cookie-store
  []
  (::system/cookie-store system))

(comment
  (start-system!)

  (stop-system!)

  (restart-system!)
  system

  ;;
  )

(comment
  (require '[clojure.repl.deps :as deps])
  (deps/sync-deps)
;;
  )
