(ns common-client.user.html
  (:require [framework-lib.core :refer [create-entity gen-table]]
            [common-client.user.entity :refer [table-conf-fn]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.functionalities :as fns]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (or (contains?
              @allowed-actions
              fns/user-create)
            (contains?
              @allowed-actions
              fns/user-read))
    {:label (get-label 21)
     :id "user-nav-id"
     :sub-menu [(when (contains?
                        @allowed-actions
                        fns/user-create)
                  {:label (get-label 4)
                   :id "user-create-nav-id"
                   :evt-fn create-entity
                   :evt-p (table-conf-fn)})
                (when (contains?
                        @allowed-actions
                        fns/user-read)
                  {:label (get-label 5)
                   :id "user-show-all-nav-id"
                   :evt-fn gen-table
                   :evt-p (table-conf-fn)})]}
   ))

