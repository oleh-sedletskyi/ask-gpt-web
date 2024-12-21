(ns gpt.static.routes
  (:require [ring.util.response :as response]))

(defn favicon-ico-handler
  [& _]
  (response/resource-response "/favicon.ico"))

(defn routes
  [_]
  [["/favicon.ico" favicon-ico-handler]])
