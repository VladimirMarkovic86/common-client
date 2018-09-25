(ns common-client.allowed-actions.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [common-middle.functionalities :as fns]
            [common-middle.request-urls :as rurls]))

(def allowed-actions
     (atom #{}))

(defn get-allowed-actions
  "Request set of allowed actions for user that is logged in"
  []
  (let [xhr (sjax
              {:url rurls/get-allowed-actions-url})
        response (get-response xhr)
        data (:data response)
        ;data (into
        ;       #{}
        ;       fns/functionalities)
        ]
    (reset!
      allowed-actions
      data))
 )

