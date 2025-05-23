(ns build
  (:require [clojure.tools.build.api :as b]))                     ; requiring tools.build

(def build-folder "target")

(defn clean [_]
  (b/delete {:path build-folder})                                 ; removing artifacts folder with (b/delete)
  (println (format "Build folder \"%s\" removed" build-folder)))

(def lib 'my/ask-gpt1)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[gpt.main]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'gpt.main}))
