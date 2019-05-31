(ns common-client.forgot-password.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [validator-lib.core :refer [validate-field]]
            [language-lib.core :refer [get-label]]
            [js-lib.core :as md]
            [common-client.forgot-password.html :as fph]
            [common-client.reset-password-code.controller :as rpcc]
            [common-middle.request-urls :as rurls]))

(defn forgot-password-error
  "Sign up error function"
  [xhr]
  (let [response (get-response xhr)
        status (:status response)
        message (:message response)
        email (md/get-by-id
                "txtEmailId")
        is-valid (atom true)]
    (validate-field
      email
      is-valid
      (get-label 58)
      true))
 )

(defn forgot-password-evt
  "Read data from sign up page and submit form"
  [cancel-fn
   cancel-evt]
  {:onclick
    {:evt-fn
      (fn []
        (let [email (md/query-selector-on-element
                      ".login"
                      "#txtEmailId")
              is-valid (atom true)]
          (validate-field
            email
            is-valid)
          (when @is-valid
            (let [email (md/get-value
                          email)]
              (ajax
                {:url rurls/forgot-password-url
                 :success-fn rpcc/reset-password-code-evt-fn
                 :error-fn forgot-password-error
                 :entity {:email email}
                 :cancel-fn cancel-fn
                 :cancel-evt cancel-evt}))
           ))
       )}})

(defn forgot-password-evt-fn
  "Sign up form with cancel events"
  [{cancel-fn :cancel-fn
    cancel-evt :cancel-evt}]
  (md/remove-element-content
    "body > div:first-child")
  (md/append-element
    "body > div:first-child"
    (fph/form-fn
      (forgot-password-evt
        cancel-fn
        cancel-evt)
      cancel-evt))
 )

