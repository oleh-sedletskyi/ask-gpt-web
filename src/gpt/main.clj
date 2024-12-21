(ns gpt.main
  (:require [gpt.system :as system]))

(defn -main []
  (system/start-system))
