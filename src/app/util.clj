(ns app.util)

(defn hook
  {:shadow.build/stage :optimize-finish}
  [build-state]
  (prn (:shadow.build/config build-state))
  build-state)
