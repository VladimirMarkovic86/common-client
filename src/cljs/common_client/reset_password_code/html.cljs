(ns common-client.reset-password-code.html
  (:require [htmlcss-lib.core :refer [gen label input form div
                                      fieldset span]]
            [validator-lib.core :refer [validate-input]]
            [language-lib.core :refer [get-label]]))

(defn form-fn
  "Generate table HTML element that contains sign up form"
  [reset-password-code-evt
   cancel-evt]
  (gen
    (form
      (div
        [(fieldset
           (label
             [(get-label 77)
              (input
                ""
                {:id "txtCodeId"
                 :type "text"
                 :placeholder (get-label 77)
                 :title (get-label 77)
                 :required true}
                {:oninput {:evt-fn validate-input}})
              (span)])
          )
         (div
           [(input
              ""
              {:type "submit"
               :value (get-label 78)
               :class "btn btn-default"}
              reset-password-code-evt)
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

