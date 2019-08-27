(ns common-client.chat.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.functionalities :as fns]
            [common-client.chat.view :as cv]))

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (contains?
          @allowed-actions
          fns/chat)
    {:label (get-label 68)
     :id "chat-nav-id"
     :evt-fn cv/chat-pure-html}))

