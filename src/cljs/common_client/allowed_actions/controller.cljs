(ns common-client.allowed-actions.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [common-middle.functionalities :as fns]))

(def allowed-actions
     (atom #{}))

(defn get-allowed-actions
  ""
  []
  (let [xhr (sjax
              {:url "/clojure/get-allowed-actions"})
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

