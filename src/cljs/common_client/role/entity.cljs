(ns common-client.role.entity
  (:require [framework-lib.core :refer [gen-table]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]))

(def entity-type
     "role")

(def functionalities
     (atom nil))

(defn form-conf-fn
  "Form configuration for role entity"
  []
  {:id :_id
   :type entity-type
   :entity-name (get-label 22)
   :fields {:role-name {:label (get-label 28)
                        :input-el "text"
                        :attrs {:required "required"}}
            :functionalities {:label (get-label 29)
                              :input-el "checkbox"
                              :attrs {:required "required"}
                              :options @functionalities}}
   :fields-order [:role-name
                  :functionalities]})

(defn columns-fn
  "Table columns for role entity"
  []
  {:projection [:role-name
                ;:functionalities
                ]
   :style
    {:role-name
      {:content (get-label 28)
       :th {:style {:width "200px"}}
       :td {:style {:width "200px"
                    :text-align "left"}}
       }
     :functionalities
      {:content (get-label 29)
       :th {:style {:width "100px"}}
       :td {:style {:width "100px"
                    :text-align "left"}}
       }}
    })

(defn query-fn
  "Table query for role entity"
  []
  {:entity-type entity-type
   :entity-filter {}
   :projection (:projection (columns-fn))
   :projection-include true
   :qsort {:role-name 1}
   :pagination true
   :current-page 0
   :rows 25
   :collation {:locale "sr"}})

(defn table-conf-fn
  "Table configuration for role entity"
  []
  {:query (query-fn)
   :columns (columns-fn)
   :form-conf (form-conf-fn)
   :actions [:details :edit :delete]
   :allowed-actions @allowed-actions
   :search-on true
   :search-fields [:role-name]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

