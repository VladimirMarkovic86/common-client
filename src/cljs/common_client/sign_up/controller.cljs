(ns common-client.sign-up.controller
  (:require [ajax-lib.core :refer [ajax get-response]]
            [utils-lib.core :as utils]
            [validator-lib.core :refer [validate-field]]
            [language-lib.core :refer [get-label]]
            [js-lib.core :as md]
            [common-client.sign-up.html :as suh]
            [common-middle.request-urls :as rurls]
            [common-middle.collection-names :refer [user-cname]]))

(defn sign-up-error
  "Sign up error function"
  [xhr]
  (let [response (get-response xhr)
        status (:status response)
        message (:message response)
        username (md/get-by-id
                   "txtUsernameId")
        email (md/get-by-id
                "txtEmailId")
        is-valid (atom true)]
    (validate-field
      username
      is-valid
      (get-label 61)
      true)
    (validate-field
      email
      is-valid
      (get-label 61)
      true))
 )

(defn sign-up-evt
  "Read data from sign up page and submit form"
  [cancel-fn]
  {:onclick
    {:evt-fn
      (fn []
        (let [username (md/query-selector-on-element
                         ".login"
                         "#txtUsernameId")
              email (md/query-selector-on-element
                      ".login"
                      "#txtEmailId")
              password (md/query-selector-on-element
                         ".login"
                         "#pswSignUpId")
              confirm-password (md/query-selector-on-element
                                 ".login"
                                 "#pswConfirmSignUpId")
              is-valid (atom true)]
          (validate-field
            username
            is-valid)
          (validate-field
            email
            is-valid)
          (validate-field
            password
            is-valid)
          (let [validity (.-validity
                           password)]
            (when (.-patternMismatch
                    validity)
              (validate-field
                password
                is-valid
                (get-label 64)
                true))
           )
          (validate-field
            confirm-password
            is-valid)
          (when @is-valid
            (validate-field
              confirm-password
              is-valid
              (get-label 60)
              (not= (md/get-value
                      password)
                    (md/get-value
                      confirm-password))
             ))
          (when @is-valid
            (let [username (md/get-value
                             username)
                  email (md/get-value
                          email)
                  password (md/get-value
                             password)]
              (ajax
                {:url rurls/sign-up-url
                 :success-fn cancel-fn
                 :error-fn sign-up-error
                 :entity {:entity-type user-cname
                          :entity {:username username
                                   :password (utils/sha256
                                               password)
                                   :email email}
                          :_id ""}}))
           ))
       )}})

(defn sign-up-evt-fn
  "Sign up form with cancel events"
  [{cancel-fn :cancel-fn
    cancel-evt :cancel-evt}]
  (md/remove-element-content
    "body > div:first-child")
  (md/append-element
    "body > div:first-child"
    (suh/form-fn
      (sign-up-evt
        cancel-fn)
      cancel-evt))
 )

