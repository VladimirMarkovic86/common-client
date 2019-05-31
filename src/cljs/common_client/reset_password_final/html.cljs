(ns common-client.reset-password-final.html
  (:require [htmlcss-lib.core :refer [gen label input form div
                                      fieldset span]]
            [validator-lib.core :refer [validate-input]]
            [language-lib.core :refer [get-label]]
            [common-client.user.entity :refer [password-pattern]]))

(defn form-fn
  "Generate table HTML element that contains sign up form"
  [reset-password-final-evt
   cancel-evt]
  (gen
    (form
      (div
        [(fieldset
           [(label
              [(get-label 80)
               (input
                 ""
                 {:id "pswNewPasswordId"
                  :type "password"
                  :placeholder (get-label 80)
                  :title (get-label 80)
                  :minlength 8
                  :maxlength 40
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
                 {:id "pswConfirmPasswordId"
                  :type "password"
                  :placeholder (get-label 20)
                  :title (get-label 20)
                  :required true}
                 {:oninput {:evt-fn validate-input}})
               (span)])]
          )
         (div
           [(input
              ""
              {:type "submit"
               :value (get-label 78)
               :class "btn btn-default"}
              reset-password-final-evt)
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

