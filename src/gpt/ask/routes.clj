(ns gpt.ask.routes
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [gpt.layout :as layout]
   [gpt.system :as-alias system]
   [hiccup2.core :as h]
   [java-time :as time]
   [markdown-to-hiccup.core :as md] ;; [gpt.middleware :as middleware]
   [org.httpkit.client :as hk-client]
   [ring.util.anti-forgery :as anti-forgery]
   [ring.util.response :as response])
  (:import
   (io.github.cdimascio.dotenv Dotenv)))

(defn get-open-ai-key [env]
  (or (System/getenv "OPEN_AI_KEY")
      (Dotenv/.get env "GPTKEY")))

(defn ask-gpt [env question]
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

(defonce answers (atom '{}))

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
                     :content)
        email (-> request :session :identity)]
    (swap! answers update-in [email] conj {:id id
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
  [_system request]
  (let [email (-> request :session :identity)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str
            (h/html
             [:html
              [:head
               [:link
                {:rel "stylesheet"
                 :href "styles.css"}]]
              (when (get-in request [:flash :layout/message])
                [:h3 {:style {:color "green"}}
                 (get-in request [:flash :layout/message])])
              [:body
               #_[:code [:div (pr-str (-> request :session))]]
               [:h2 "Ask GPT"]
               [:form {:method "post"
                       :action "/gpt/ask"}
                (h/raw (anti-forgery/anti-forgery-field))
                [:label {:for "question"} "Question"]
                [:br]
                [:textarea {:id "question" :name "question" :type "text" :rows "10" :placeholder "Type your question.."}]
                [:br]
                [:div
                 [:input {:type "submit" :name "default-submit"}]
                 [:input {:type "submit" :name "check-en-grammar" :value "Check English grammar"}]]]
               [:br]
               [:h1 "Previous answers:"]
               [:div
                (for [answer (get @answers email [])]
                  [:div {:style {:border "1px solid grey"
                                 :padding "5px"}}
                   [:div (convert-unix-to-readable (:created answer)) " - " (:question answer)]
                   [:hr]
                   [:div (->> (:content answer)
                              md/md->hiccup
                              md/component)]])]]]))}))

#_(defn read-allowed-emails [env])

(def read-allowed-emails
  (memoize
   (fn read-allowed-emails [env]
     (-> (Dotenv/.get env "allowed-emails")
         (str/split #"\s")
         set))))

(defn wrap-auth [{::system/keys [env]} handler]
  (fn [request]
    (let [email (-> request :session :identity)]
      ;; (pprint/pprint (:session request))
      (if (contains? (read-allowed-emails env) email) #_true
          (do
            (log/info (str "Access granted for: " email))
            (handler request))
          (do
            (log/warn (str "Access denied for: '" email "'"))
            (-> (response/redirect "/")
                (layout/flash-message "Please, login")))))))

(defn routes
  [system]
  [""
   #_["/session" ;; test route to check whether session is set
      ["" p {:get {:handler (fn [request]
                              (-> request
                                  (assoc-in [:session :email1] "ololo")))}}]
      ["/get" {:get {:handler (fn [request]
                                {:status 200
                                 :headers {"Content-Type" "text/html"}
                                 :body (with-out-str (pprint/pprint (:session request)))})}}]]
   ["/gpt"
    {:middleware [(partial wrap-auth system)]}
    ["" {:get {:handler (partial #'gpt-handler system)}}]
    ["/ask" {:post {:handler (partial #'gpt-ask-handler system)}}]]])

(comment
  (let [env (::system/env user/system)]
    (-> (Dotenv/.get env "allowed-emails")
        (str/split #"\s")
        set))
  (swap! answers update-in [:test-user] conj {:k :test})
  ;;
  )
