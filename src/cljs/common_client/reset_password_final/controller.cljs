(ns common-client.reset-password-final.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [validator-lib.core :refer [validate-field]]
            [language-lib.core :refer [get-label]]
            [js-lib.core :as md]
            [common-client.reset-password-final.html :as rpfh]
            [common-middle.request-urls :as rurls]
            [utils-lib.core :as utils]))

(defn reset-password-final-error
  "Sign up error function"
  [xhr]
  (let [response (get-response xhr)
        status (:status response)
        message (:message response)
        new-password-element (md/get-by-id
                               "pswNewPasswordId")
        confirm-password-element (md/get-by-id
                                   "pswConfirmPasswordId")
        is-valid (atom true)]
    (validate-field
      new-password-element
      is-valid
      (get-label 79)
      true)
    (validate-field
      confirm-password-element
      is-valid
      (get-label 79)
      true))
 )

(defn reset-password-final-evt
  "Read data from sign up page and submit form"
  [cancel-fn
   code]
  {:onclick
    {:evt-fn
      (fn []
        (let [new-password-element (md/query-selector-on-element
                                     ".login"
                                     "#pswNewPasswordId")
              confirm-password-element (md/query-selector-on-element
                                         ".login"
                                         "#pswConfirmPasswordId")
              is-valid (atom true)]
          (validate-field
            new-password-element
            is-valid)
          (let [validity (.-validity
                           new-password-element)]
            (when (.-patternMismatch
                    validity)
              (validate-field
                new-password-element
                is-valid
                (get-label 64)
                true))
           )
          (validate-field
            confirm-password-element
            is-valid)
          (when @is-valid
            (validate-field
              confirm-password-element
              is-valid
              (get-label 60)
              (not= (md/get-value
                      new-password-element)
                    (md/get-value
                      confirm-password-element))
             ))
          (when @is-valid
            (let [new-password (md/get-value
                                 new-password-element)]
              (ajax
                {:url rurls/reset-password-final-url
                 :success-fn cancel-fn
                 :error-fn reset-password-final-error
                 :entity {:new-password (utils/sha256
                                          new-password)
                          :uuid code}}))
           ))
       )}})

(defn reset-password-final-evt-fn
  "Sign up form with cancel events"
  [xhr
   {cancel-fn :cancel-fn
    cancel-evt :cancel-evt
    {code :uuid} :entity}]
  (md/remove-element-content
    "body > div:first-child")
  (md/append-element
    "body > div:first-child"
    (rpfh/form-fn
      (reset-password-final-evt
        cancel-fn
        code)
      cancel-evt))
 )

