(ns gpt.main
  (:require [gpt.system :as system])
  (:gen-class))

(defn -main []
  (system/start-system))
