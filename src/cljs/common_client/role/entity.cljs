(ns common-client.role.entity
  (:require [framework-lib.core :refer [gen-table]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.collection-names :refer [role-cname]]
            [common-middle.role.entity :as cmre]))

(def entity-type
     role-cname)

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
                        :attrs {:placeholder (get-label 28)
                                :required true}}
            :functionalities {:label (get-label 29)
                              :input-el "select"
                              :options @functionalities
                              :attrs {:required true
                                      :multiple true}}
            }
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
       :th {:style {:width "70%"}}
       :td {:style {:width "70%"
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
   :rows (cmre/calculate-rows)
   :collation {:locale "sr"}})

(defn table-conf-fn
  "Table configuration for role entity"
  []
  {:preferences cmre/preferences
   :query-fn query-fn
   :query (query-fn)
   :columns (columns-fn)
   :form-conf (form-conf-fn)
   :actions [:details :edit :delete]
   :allowed-actions @allowed-actions
   :reports-on true
   :search-on true
   :search-fields [:role-name]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

