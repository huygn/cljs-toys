(ns app.build
  (:require
   [shadow.cljs.devtools.api :as shadow]
   [clojure.java.shell :refer (sh)]))

(defn release []
  (shadow/release :app)
  (println "post build")
  (sh "echo" "release"))
