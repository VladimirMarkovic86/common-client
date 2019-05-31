(ns common-client.forgot-password.html
  (:require [htmlcss-lib.core :refer [gen label input form div
                                      fieldset span]]
            [validator-lib.core :refer [validate-input]]
            [language-lib.core :refer [get-label]]))

(defn form-fn
  "Generate table HTML element that contains sign up form"
  [forgot-password-evt
   cancel-evt]
  (gen
    (form
      (div
        [(fieldset
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
          )
         (div
           [(input
              ""
              {:type "submit"
               :value (get-label 67)
               :class "btn btn-default"}
              forgot-password-evt)
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

