(ns app.pages.contact_form
  (:require [rum.core :as rum]
            ["final-form" :as fform]))

(rum/defcs form-component <
  rum/reactive
  (let [form-state (atom {})
        field-state (atom {})
        form (fform/createForm #js {:onSubmit (fn [v f]
                                                (js/console.log v f)
                                                (js/Promise. (fn [resolve]
                                                               (js/setTimeout
                                                                #(do (js/console.log "submitted")
                                                                     (resolve))
                                                                500))))})
        unsubscribe (.subscribe form
                                #(reset! form-state %)
                                #js {:active true :pristine true :submitting true :values true})
        unsubscribeFields (map (fn [field-name]
                                 (.registerField form
                                                 field-name
                                                 #(swap! field-state assoc field-name %)
                                                 #js {:active true
                                                      :dirty true
                                                      :touched true
                                                      :valid true
                                                      :value true}))
                               (clj->js [:firstName :lastName]))]
    {:will-mount (fn [state]
                   (js/console.log (apply hash-map
                                          form-state field-state form unsubscribe unsubscribeFields))
                   (assoc state
                          :form form
                          :form-state form-state
                          :field-state field-state))
     :did-mount (fn [state]
                  (js/console.log state)
                  state)})
  [state]
  (let [form (:form state)
        form-state (rum/react (:form-state state))
        field-state (rum/react (:field-state state))
        onsubmit (fn [e]
                   (.preventDefault e)
                   (.submit form))]
    [:form {:on-submit onsubmit}
     [:input {:class "border"
              :name "firstName"
              :on-blur #(.blur (get field-state "firstName"))
              :on-change #(.change (get field-state "firstName") (-> % .-target .-value))}]
     [:input {:class "border"
              :name "lastName"
              :on-blur #(.blur (get field-state "lastName"))
              :on-change #(.change (get field-state "lastName") (-> % .-target .-value))}]
     [:button {:class "text-white bg-blue px-4 py-2"
               :type "submit"
               :disabled (.-submitting form-state)}
      "Submit"]]))
