(ns app.pages.contact_form
  (:require [rum.core :as rum]
            ["final-form" :as fform]))

(defn make-field [form]
  (rum/defcs field <
    rum/reactive
    {:will-mount (fn [state]
                   (let [field-name (:name (first (:rum/args state)))
                         field-state-atom (atom {})
                         unsubscribe-field (.registerField form
                                                           field-name
                                                           #(reset! field-state-atom %)
                                                           #js {:active true
                                                                :dirty true
                                                                :touched true
                                                                :valid true
                                                                :value true})]
                     (js/console.log "will-mount")
                     (assoc state
                            :unsubscribe unsubscribe-field
                            :field-state field-state-atom)))
     :will-unmount (fn [state]
                     ((:unsubscribe state))
                     state)}
    [state {:keys [name]} render-field]
    (let [field (rum/react (:field-state state))]
      (js/console.log "field-render" field)
      (render-field {:field-state field
                     :input {:name name
                             :value (-> (.-value field) (or ""))
                             :on-blur #(.blur field)
                             :on-focus #(.focus field)
                             :on-change #(.change field (-> % .-target .-value))}}))))

(rum/defcs form <
  rum/reactive
  {:init (fn [state]
           (let [form (fform/createForm
                       #js {:onSubmit (fn [v f]
                                        (js/console.log v f)
                                        (js/Promise. (fn [resolve]
                                                       (js/setTimeout
                                                        #(do (js/console.log "submitted")
                                                             (resolve))
                                                        500))))})]
             (assoc state :form form)))
   :will-mount (fn [state]
                 (let [form (:form state)
                       form-state (atom {})
                       unsubscribe (.subscribe form
                                               #(reset! form-state %)
                                               #js {:active true :pristine true :submitting true :values true})
                       render-field (make-field form)]
                   (assoc state
                          :form-state form-state
                          :render-field render-field)))
   :did-mount (fn [state]
                  ; (js/console.log state)
                state)}
  [state render]
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
  (form (fn [{:keys [form-state submit! field]}]
          [:form {:on-submit submit!}
           (field {:name "firstName"}
                  (fn [{:keys [input]}]
                    [:input (merge input {:class "border"})]))])))
