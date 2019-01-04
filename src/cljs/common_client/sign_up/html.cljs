(ns common-client.sign-up.html
  (:require [htmlcss-lib.core :refer [gen label input form div
                                      fieldset span]]
            [validator-lib.core :refer [validate-input]]
            [language-lib.core :refer [get-label]]
            [common-client.user.entity :refer [password-pattern]]))

(defn form-fn
  "Generate table HTML element that contains sign up form"
  [sign-up-evt
   cancel-evt]
  (gen
    (form
      (div
        [(fieldset
           [(label
              [(get-label 19)
               (input
                 ""
                 {:id "txtUsernameId"
                  :type "text"
                  :placeholder (get-label 19)
                  :title (get-label 19)
                  :required true}
                 {:oninput {:evt-fn validate-input}})
               (span)])
            (label
              [(get-label 14)
               (input
                 ""
                 {:id "txtEmailId"
                  :type "email"
                  :placeholder (get-label 14)
                  :title (get-label 14)
                  :required true}
                 {:oninput {:evt-fn validate-input}})
               (span)])
            (label
              [(get-label 15)
               (input
                 ""
                 {:id "pswSignUpId"
                  :type "password"
                  :placeholder (get-label 15)
                  :minlength 8
                  :maxlength 40
                  :title (get-label 15)
                  :pattern password-pattern
                  :required true}
                 {:oninput {:evt-fn validate-input
                            :evt-p {:pattern-mismatch (get-label 64)}}
                  })
               (span)])
            (label
              [(get-label 20)
               (input
                 ""
                 {:id "pswConfirmSignUpId"
                  :type "password"
                  :placeholder (get-label 20)
                  :minlength 8
                  :maxlength 40
                  :title (get-label 20)
                  :pattern password-pattern
                  :required true}
                 {:oninput {:evt-fn validate-input
                            :evt-p {:pattern-mismatch (get-label 64)}}
                  })
               (span)])]
          )
         (div
           [(input
              ""
              {:type "submit"
               :value (get-label 18)
               :class "btn btn-default"}
              sign-up-evt)
            (input
              ""
              {:type "button"
               :value (get-label 12)
               :class "btn"}
              cancel-evt)])]
       )
     {:class "login"
      :onsubmit "return false"
      :novalidate true
      :autocomplete "off"}))
 )

