(ns app.pages.contact_form
  (:require [rum.core :as rum]
            ["final-form" :as fform]))

(def form-subscription-default {:active true
                                :dirty true
                                :dirtySinceLastSubmit true
                                :error true
                                :errors true
                                :hasSubmitErrors true
                                :hasValidationErrors true
                                :initialValues true
                                :invalid true
                                :pristine true
                                :submitError true
                                :submitErrors true
                                :submitFailed true
                                :submitting true
                                :submitSucceeded true
                                :touched true
                                :valid true
                                :validating true
                                :values true
                                :visited true})

(def field-subscription-default {:active true
                                 :data true
                                 :dirty true
                                 :dirtySinceLastSubmit true
                                 :error true
                                 :initial true
                                 :invalid true
                                 :length true
                                 :pristine true
                                 :submitError true
                                 :submitFailed true
                                 :submitSucceeded true
                                 :touched true
                                 :valid true
                                 :value true
                                 :visited true})

(defn create-field [form]
  (rum/defcs field <
    rum/reactive
    {:init (fn [state]
             (let [opt (-> state :rum/args first)
                   field-name (:name opt)
                   subscription (-> opt :subscription (or field-subscription-default))
                   field-state (atom {})
                   unsubscribe (.registerField form
                                               field-name
                                               #(reset! field-state
                                                        (js->clj % :keywordize-keys true))
                                               (clj->js subscription))]
               (assoc state
                      :unsubscribe unsubscribe
                      :field-state field-state)))
     :will-unmount (fn [state]
                     ((:unsubscribe state))
                     state)}

    [state {:keys [name]} render]
    (let [field (rum/react (:field-state state))
          {:keys [value change focus blur]} field
          meta (select-keys field [:active
                                   :data
                                   :dirty
                                   :dirtySinceLastSubmit
                                   :error
                                   :initial
                                   :invalid
                                   :pristine
                                   :submitError
                                   :submitFailed
                                   :submitSucceeded
                                   :touched
                                   :valid
                                   :visited])]
      (render {:input {:name name
                       :value (or value "")
                       :on-change #(-> % .-target .-value change)
                       :on-focus #(focus)
                       :on-blur #(blur)}
               :meta meta}))))

(defn form-mixin [form-opt]
  {:init (fn [state]
           (let [on-submit (:on-submit form-opt)
                 initial-values (:initial-values form-opt)
                 validate (:validate form-opt)
                 form (fform/createForm (clj->js {:onSubmit on-submit
                                                  :initialValues initial-values
                                                  :validate validate}))]
             (assoc state :form/form form)))
   :will-mount (fn [state]
                 (let [form (:form/form state)
                       form-state (atom {})
                       form-subscription (-> form-opt
                                             :subscription
                                             (or form-subscription-default))
                       unsubscribe-form (.subscribe form
                                                    #(reset!
                                                      form-state
                                                      (js->clj % :keywordize-keys true))
                                                    (clj->js form-subscription))]
                   (assoc state
                          :form/state form-state
                          :form/unsubscribe unsubscribe-form
                          :form/submit! (fn [e]
                                          (.preventDefault e)
                                          (.submit form))
                          :form/render-field (create-field form))))
   :will-unmount (fn [state]
                   ((:form/unsubscribe state))
                   state)})

(rum/defcs component <
  rum/reactive
  (form-mixin {:on-submit #(js/console.log %)})
  [state]
  (let [form-state (-> state :form/state rum/react)
        submit! (:form/submit! state)
        field (:form/render-field state)]
    (js/console.log "form-state" form-state)
    [:form {:on-submit submit!}
     (field {:name "firstName"}
            (fn [{:keys [input meta]}]
              (js/console.log "field" input meta)
              [:input (merge {:class "border"} input)]))]))
