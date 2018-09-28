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

(defn make-field [form]
  (rum/defcs field <
    rum/reactive
    {:init (fn [state]
             (let [field-name (-> state :rum/args first :name)
                   subscription (-> state :rum/args first :subscription (or field-subscription-default))
                   field-state (atom {})
                   unsubscribe (.registerField form
                                               field-name
                                               #(reset! field-state %)
                                               (clj->js subscription))]
               (assoc state
                      :unsubscribe unsubscribe
                      :field-state field-state)))
     :will-unmount (fn [state] ((:unsubscribe state)) state)}

    [state {:keys [name]} render]
    (let [field (rum/react (:field-state state))]
      (js/console.log "field-render" field)
      (render {:field-state field
               :input {:name name
                       :value (-> (.-value field) (or ""))
                       :on-blur #(.blur field)
                       :on-focus #(.focus field)
                       :on-change #(.change field (-> % .-target .-value))}}))))

(rum/defcs form <
  rum/reactive
  {:init (fn [state]
           (let [on-submit (-> state :rum/args first :on-submit)
                 form (fform/createForm
                       #js {:onSubmit on-submit})]
             (assoc state :form form)))
   :will-mount (fn [state]
                 (let [form (:form state)
                       form-state (atom {})
                       subscription (-> state :rum/args first :subscription (or form-subscription-default))
                       unsubscribe (.subscribe form
                                               #(reset! form-state
                                                        (js->clj % :keywordize-keys true))
                                               (clj->js subscription))
                       render-field (make-field form)]
                   (assoc state
                          :form-state form-state
                          :render-field render-field)))
   :will-unmount (fn [state] ((:unsubscribe state)) state)}

  [state opt render]
  (let [form (:form state)
        form-state (rum/react (:form-state state))
        submit! (fn [e]
                  (.preventDefault e)
                  (.submit form))
        render-field (:render-field state)]
    (js/console.log "form render" form-state)
    (render {:form form
             :form-state form-state
             :submit! submit!
             :field render-field})))

(rum/defc component []
  (form {:on-submit (fn [v f]
                      (js/console.log v f)
                      (js/Promise. (fn [resolve]
                                     (js/setTimeout
                                      #(do (js/console.log "submitted")
                                           (resolve))
                                      500))))}
        (fn [{:keys [form-state submit! field]}]
          [:form {:on-submit submit!}
           (field {:name "firstName"}
                  (fn [{:keys [input]}]
                    [:input (merge input {:class "border"})]))
           [:button {:type "submit"
                     :disabled (:submitting form-state)
                     :class "px-3 py-2 text-white bg-blue rounded-sm"}
            "Submit"]])))
