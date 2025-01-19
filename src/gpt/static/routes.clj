(ns gpt.static.routes
  (:require [ring.util.response :as response]))

(defn favicon-ico-handler
  [& _]
  (response/resource-response "/favicon.ico"))

(defn styles-handler
  [& _]
  (response/resource-response "styles.css"))

(defn routes
  [_]
  [["/favicon.ico" favicon-ico-handler]
   ["/styles.css" styles-handler]])
