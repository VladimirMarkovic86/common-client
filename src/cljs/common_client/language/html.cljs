(ns common-client.language.html
  (:require [framework-lib.core :refer [create-entity gen-table]]
            [common-client.language.entity :refer [table-conf-fn]]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.functionalities :as fns]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (or (contains?
              @allowed-actions
              fns/language-create)
            (contains?
              @allowed-actions
              fns/language-read))
    {:label (get-label 23)
     :id "language-nav-id"
     :sub-menu [(when (contains?
                        @allowed-actions
                        fns/language-create)
                  {:label (get-label 4)
                   :id "language-create-nav-id"
                   :evt-fn create-entity
                   :evt-p (table-conf-fn)})
                (when (contains?
                        @allowed-actions
                        fns/language-read)
                  {:label (get-label 5)
                   :id "language-show-all-nav-id"
                   :evt-fn gen-table
                   :evt-p (table-conf-fn)})]}
   ))

