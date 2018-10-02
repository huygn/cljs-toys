(ns app.components.core
  (:require [rum.core :as rum]
            [sablono.util]))

(rum/defc with-header [component]
  [:div
   [:nav.h-16.flex.items-center.mb-16
    [:a {:href "/"} "Back"]
    [:a {:href "/form"} "Form"]]
   (component)])

(defn adapt-react-class [react-class]
  (fn [& args]
    (let [[opts children] (if (map? (first args))
                            [(first args) (rest args)]
                            [{} args])
          type# (first children)]
      (let [new-children (if (sequential? type#)
                           [(sablono.interpreter/interpret children)]
                           children)]

        (apply js/React.createElement react-class
               (clj->js (sablono.util/html-to-dom-attrs opts)) new-children)))))

(defn loadable
  "Returns a Rum component. It will render the provided get-component's result
  if it's loaded successfully."
  [get-component render-component]
  (rum/defcs loadable-component <
    (rum/local false :loaded)
    {:did-mount (fn [state]
                  (let [loaded (:loaded state)]
                    (-> (get-component)
                        (.then #(reset! loaded true))))
                  state)}
    [{loaded :loaded}]
    (when (true? @loaded) (render-component))))
