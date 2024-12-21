(ns gpt.ask.routes
  (:require [org.httpkit.client :as hk-client]
            [clojure.edn :as edn]
            [ring.util.anti-forgery :as anti-forgery]
            [ring.util.response :as response]
            [hiccup2.core :as h]
            [java-time :as time]
            [gpt.system :as-alias system]
            [markdown-to-hiccup.core :as md]
            [gpt.middleware :as middleware]
            [cheshire.core :as json])
  (:import (io.github.cdimascio.dotenv Dotenv)))

(defn get-open-ai-key [env]
  (or (System/getenv "OPEN_AI_KEY")
      (Dotenv/.get env "GPTKEY")))

(defn ask-gpt [env question]
  (prn :key (get-open-ai-key env))
  (->
   @(hk-client/post "https://api.openai.com/v1/chat/completions"
                    {:headers
                     {"Content-Type" "application/json"
                      "Authorization" (format "Bearer %s" (get-open-ai-key env))}
                     :body
                     (json/encode
                      {:model "gpt-4o-mini"
                       :messages [{:role "system",
                                   :content "You are a helpful assistant."},
                                  {:role "user",
                                   :content question}]
                       "temperature" 0.7})})
   :body
   (json/decode keyword)))

(defonce responses (atom '()))

(defn gpt-ask-handler
  [{::system/keys [env]} request]
  (let [{:keys [question check-en-grammar]} (:params request)
        q (if check-en-grammar
            (str "Check the English grammar for the following:\n" question)
            question)
        response (ask-gpt env q)
        id (:id response)
        created (:created response)
        content (->> (:choices response)
                     first
                     :message
                     :content)]
    (swap! responses conj {:id id
                           :created created
                           :content content
                           :question q})
    (response/redirect "/gpt")))

(defn convert-unix-to-readable [timestamp]
  (when-not (nil? timestamp)
    (->> (java.time.Instant/ofEpochMilli timestamp)
         str
         time/offset-date-time
         (time/format "d/MMM/y H:m:s"))))

(defn gpt-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str
          (h/html
           [:html
            [:body
             [:h1 "Ask GPT"]
             [:form {:method "post"
                     :action "/gpt/ask"}
              (h/raw (anti-forgery/anti-forgery-field))
              [:label {:for "question"} "Question"]
              [:br]
              [:textarea {:id "question" :name "question" :type "text" :cols "60" :rows "10" :placeholder "Type your question.."}]
              [:br]
              [:div
               [:input {:type "submit" :name "default-submit"}]
               [:input {:type "submit" :name "check-en-grammar" :value "Check English grammar"}]]]
             [:br]
             [:h1 "Previous answers:"]
             [:div
              (for [response @responses]
                [:div {:style {:border "1px solid grey"
                               :padding "5px"}}
                 [:div (convert-unix-to-readable (:created response)) " - " (:question response)]
                 [:hr]
                 [:div (->> (:content response)
                            md/md->hiccup
                            md/component)]])]]]))})

(defn routes
  [system]
  [""
   {:middleware (middleware/standard-html-route-middleware system)}
   ["/gpt" {:get {:handler (partial #'gpt-handler system)}}]
   ["/gpt/ask" {:post {:handler (partial #'gpt-ask-handler system)}}]])

(comment
  (ask-gpt "how many languages do you support?")
  (swap! responses conj {})
  ;;
  )
