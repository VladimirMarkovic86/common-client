(ns common-client.user.entity
  (:require [ajax-lib.core :refer [sjax get-response]]
            [utils-lib.core :as utils]
            [js-lib.core :as md]
            [htmlcss-lib.core :refer [div label input span]]
            [framework-lib.core :refer [gen-table]]
            [validator-lib.core :refer [validate-input]]
            [language-lib.core :refer [get-label]]
            [common-middle.user.entity :as cmue]
            [common-middle.request-urls :as rurls]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.collection-names :refer [user-cname
                                                    role-cname]]))

(def entity-type
     user-cname)

(defn get-roles
  "Get roles for logged in user"
  []
  (let [xhr (sjax
              {:url rurls/get-entities-url
               :entity {:entity-type role-cname
                        :entity-filter {}
                        :projection [:_id :role-name]
                        :projection-include true
                        :qsort {:role-name 1}
                        :pagination false
                        :collation {:locale "sr"}}
               })
        response (get-response
                   xhr)
        data (:data response)
        options (atom [])]
    (doseq [{op-value :_id
             op-label :role-name} data]
      (swap!
        options
        conj
        [op-label
         op-value]))
    @options))

(def password-pattern
     "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\.])[A-Za-z\\d@$!%*?&\\.]{8,40}")

(defn sub-form
  "Generate ingredients sub form"
  [data
   attrs]
  (let [disabled (:disabled attrs)
        new-attrs {:id "pswFormId"
                   :type "password"
                   :placeholder (get-label 15)
                   :minlength 8
                   :maxlength 40
                   :title (get-label 15)
                   :pattern password-pattern
                   :required true}
        new-attrs (if disabled
                    (assoc
                      new-attrs
                      :disabled
                      true)
                    new-attrs)]
    [(div
       (label
         [(get-label 15)
          (input
            ""
            new-attrs
            {:oninput {:evt-fn validate-input
                       :evt-p {:pattern-mismatch (get-label 64)}}
             })
          (span)])
      )])
 )

(defn read-form
  "Read meal form"
  []
  (let [password-change-el (md/query-selector-on-element
                             ".entity"
                             "#pswFormId")
        password-change-value (md/get-value
                                password-change-el)]
    (utils/sha256
      password-change-value))
 )

(defn validate-form
  "Validate password special field"
  [validate-field-fn
   is-valid]
  (let [input-element (md/query-selector-on-element
                        ".entity"
                        "#pswFormId")]
    (validate-field-fn
      input-element
      is-valid))
 )

(defn form-conf-fn
  "Form configuration for user entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 21)
   :fields {:username {:label (get-label 19)
                       :input-el "text"
                       :attrs {:required true
                               :placeholder (get-label 19)}}
            :password {:label (get-label 15)
                       :input-el "sub-form"
                       :sub-form-fieldset sub-form
                       :sub-form-fieldset-read read-form
                       :sub-form-validation validate-form}
            :email {:label (get-label 14)
                    :input-el "email"
                    :attrs {:required true
                            :placeholder (get-label 14)}}
            :roles {:label (get-label 30)
                    :input-el "select"
                    :options get-roles
                    :attrs {:required true
                            :multiple true}}}
   :fields-order [:username
                  :email
                  :password
                  :roles]
   :projection [:username
                :email
                ;:password
                :roles]
   :projection-include true})

(defn columns-fn
  "Table columns for user entity"
  []
  {:projection [:username
                ;:password
                :email
                ]
   :style
    {:username
      {:content (get-label 19)
       :th {:style {:width "35%"}}
       :td {:style {:width "35%"
                    :text-align "left"}}
       }
     :password
      {:content (get-label 15)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }
     :email
      {:content (get-label 14)
       :th {:style {:width "35%"}}
       :td {:style {:width "35%"
                    :text-align "left"}}
       }}
    })

(defn query-fn
  "Table query for user entity"
  []
  {:entity-type entity-type
   :entity-filter {}
   :projection (:projection (columns-fn))
   :projection-include  true
   :qsort {:username 1}
   :pagination true
   :current-page 0
   :rows cmue/rows
   :collation {:locale "sr"}})

(defn table-conf-fn
  "Table configuration for user entity"
  []
  {:query (query-fn)
   :columns (columns-fn)
   :form-conf (form-conf-fn)
   :actions [:details :edit :delete]
   :allowed-actions @allowed-actions
   :reports-on true
   :search-on true
   :search-fields [:username :email]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

