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

(defn with-form [form-opt fields]
  {:init (fn [state]
           (let [on-submit (:on-submit form-opt)
                 initial-values (-> form-opt :initial-values clj->js)
                 validate (:validate form-opt)
                 form (fform/createForm #js {:onSubmit on-submit
                                             :initialValues initial-values
                                             :validate validate})]
             (assoc state :form/form form)))
   :will-mount (fn [state]
                 (let [form (:form/form state)
                       form-state (atom {})
                       field-state (atom {})
                       form-subscription (-> form-opt
                                             :subscription
                                             (or form-subscription-default))
                       unsubscribe-form (.subscribe form
                                                    #(reset!
                                                      form-state
                                                      (js->clj % :keywordize-keys true))
                                                    (clj->js form-subscription))
                       unsubscribe-fields (doall
                                           (map (fn [{name :name, sub :subscription}]
                                                  (.registerField
                                                   form
                                                   name
                                                   #(swap! field-state assoc name
                                                           (js->clj % :keywordize-keys true))
                                                   (-> sub
                                                       (or field-subscription-default)
                                                       clj->js)))
                                                fields))]
                   (assoc state
                          :form/state form-state
                          :fields/state field-state
                          :form/unsubscribe unsubscribe-form
                          :fields/unsubscribe unsubscribe-fields
                          :form/submit! (fn [e]
                                          (.preventDefault e)
                                          (.submit form))
                          :form/field (fn [name]
                                        (let [field (get @field-state name)
                                              {:keys [value change focus blur]} field
                                              {:keys [active
                                                      data
                                                      dirty
                                                      dirtySinceLastSubmit
                                                      error
                                                      initial
                                                      invalid
                                                      pristine
                                                      submitError
                                                      submitFailed
                                                      submitSucceeded
                                                      touched
                                                      valid
                                                      visited] :as meta} field]
                                          {:input {:name name
                                                   :value (or value "")
                                                   :on-change #(-> % .-target .-value change)
                                                   :on-focus #(focus)
                                                   :on-blur #(blur)}
                                           :meta meta})))))
   :will-unmount (fn [state]
                   ((:form/unsubscribe state))
                   (doseq [unsub (:fields/unsubscribe state)] (unsub))
                   state)})

(rum/defcs component <
  rum/reactive
  (with-form
    {:on-submit #(js/console.log %)}
    [{:name "firstName"}])
  [state]
  (let [form (:form/form state)
        form-state (rum/react (:form/state state))
        submit! (:form/submit! state)
        field (:form/field state)]
    (js/console.log (field "firstName"))
    [:form {:on-submit submit!}
     [:input (merge {:class "border"}
                    (:input (field "firstName")))]]))
