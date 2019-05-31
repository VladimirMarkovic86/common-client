(ns common-client.reset-password-code.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [validator-lib.core :refer [validate-field]]
            [language-lib.core :refer [get-label]]
            [js-lib.core :as md]
            [common-client.reset-password-code.html :as rpch]
            [common-client.reset-password-final.controller :as rpfc]
            [common-middle.request-urls :as rurls]))

(defn reset-password-code-error
  "Sign up error function"
  [xhr]
  (let [response (get-response xhr)
        status (:status response)
        message (:message response)
        code (md/get-by-id
               "txtCodeId")
        is-valid (atom true)]
    (validate-field
      code
      is-valid
      (get-label 79)
      true))
 )

(defn reset-password-code-evt
  "Read data from sign up page and submit form"
  [cancel-fn
   cancel-evt]
  {:onclick
    {:evt-fn
      (fn []
        (let [code (md/query-selector-on-element
                     ".login"
                     "#txtCodeId")
              is-valid (atom true)]
          (validate-field
            code
            is-valid)
          (when @is-valid
            (let [code (md/get-value
                         code)]
              (ajax
                {:url rurls/reset-password-code-url
                 :success-fn rpfc/reset-password-final-evt-fn
                 :error-fn reset-password-code-error
                 :entity {:uuid code}
                 :cancel-fn cancel-fn
                 :cancel-evt cancel-evt}))
           ))
       )}})

(defn reset-password-code-evt-fn
  "Sign up form with cancel events"
  [xhr
   {cancel-fn :cancel-fn
    cancel-evt :cancel-evt}]
  (md/remove-element-content
    "body > div:first-child")
  (md/append-element
    "body > div:first-child"
    (rpch/form-fn
      (reset-password-code-evt
        cancel-fn
        cancel-evt)
      cancel-evt))
 )

