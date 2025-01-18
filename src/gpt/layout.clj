(ns gpt.layout)

(defn flash-message [req msg]
  (assoc-in req [:flash :layout/message] msg))
