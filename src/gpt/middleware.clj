(ns gpt.middleware
  (:require
   [ring.middleware.defaults :as middleware-defaults]
   [gpt.system :as-alias system]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.default-charset :refer [wrap-default-charset]]
   [ring.middleware.flash :refer [wrap-flash]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.nested-params :refer [wrap-nested-params]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.x-headers :as x]))

(defn standard-html-route-middleware
  [{::system/keys [cookie-store]}]
  [;; Prevents "media type confusion" attacks
   #(x/wrap-content-type-options % :nosniff)
   ;; Prevents "clickjacking" attacks
   #(x/wrap-frame-options % :sameorigin)
   ;; Returns "304 Not Modified" if appropriate
   wrap-not-modified
   ;; Adds "; charset=utf-8" to responses if none specified
   #(wrap-default-charset % "utf-8")
   ;; Guesses an appropriate Content-Type if none set
   wrap-content-type
   ;; Parses out cookies from the request
   wrap-cookies
   ;; Parses out urlencoded form and url parameters
   wrap-params
   ;; Parses out multipart params.
   ;; Useful for things like file uploads
   wrap-multipart-params
   ;; Handles "multi-value" form parameters
   wrap-nested-params
   ;; Turns any string keys in :params into keywords
   wrap-keyword-params
   ;; Handles reading and writing "session data"
   #(wrap-session % {:cookie-attrs {:http-only true}
                     :store cookie-store})
   ;; Handles "flash" data which is around only until the
   ;; immediate next request.
   wrap-flash
   ;; Ensures that POST requests contain an anti-forgery token
   wrap-anti-forgery]
  #_[#(middleware-defaults/wrap-defaults % middleware-defaults/site-defaults)])
