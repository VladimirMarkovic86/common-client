(ns common-client.user.entity
  (:require [ajax-lib.core :refer [sjax get-response]]
            [framework-lib.core :refer [gen-table]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]))

(def entity-type
     "user")

(defn get-roles
  ""
  []
  (let [xhr (sjax
              {:url "/clojure/get-entities"
               :entity {:entity-type "role"
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

(def form-conf
     {:id :_id
      :type entity-type
      :entity-name (get-label 21)
      :fields {:username {:label (get-label 19)
                          :input-el "text"}
               :password {:label (get-label 15)
                          :input-el "password"}
               :email {:label (get-label 14)
                       :input-el "email"}
               :roles {:label (get-label 30)
                       :input-el "checkbox"
                       :options get-roles}}
      :fields-order [:username
                     :password
                     :email
                     :roles]})

(def columns
     {:projection [:username
                   ;:password
                   :email
                   ]
      :style
       {:username
         {:content (get-label 19)
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
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
          :th {:style {:width "100px"}}
          :td {:style {:width "100px"
                       :text-align "left"}}
          }}
       })

(def query
     {:entity-type entity-type
      :entity-filter {}
      :projection (:projection columns)
      :projection-include  true
      :qsort {:username 1}
      :pagination true
      :current-page 0
      :rows 25
      :collation {:locale "sr"}})

(defn table-conf-fn
  ""
  []
  {:query query
   :columns columns
   :form-conf form-conf
   :actions [:details :edit :delete]
   :allowed-actions @allowed-actions
   :search-on true
   :search-fields [:username :email]
   :render-in ".content"
   :table-class "entities"
   :table-fn gen-table})

