(ns common-client.chat.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [common-middle.request-urls :as rurls]
            [js-lib.core :as md]
            [clojure.string :as cstring]
            [language-lib.core :refer [get-label]]))

(defn get-chat-users
  "Get all users for chat"
  [logged-in-username]
  (let [xhr (sjax
              {:url rurls/get-chat-users-url})
        response (get-response
                   xhr)
        users (:data response)
        users-reduced (reduce
                        (fn [acc
                             elem]
                          (let [username (:username elem)]
                            (if-not (= logged-in-username
                                         username)
                              (conj
                                acc
                                elem)
                              acc))
                         )
                        []
                        users)]
    users-reduced))

(defn get-chat-history
  "Get chat history for selected user and logged in user"
  [logged-in-username]
  (when-let [selected-username-el (md/query-selector-on-element
                                    ".chat"
                                    "#chat-users")]
    (let [selected-username (.-innerHTML
                              (aget
                                (.-selectedOptions
                                  selected-username-el)
                                0))
          messages (atom [])]
      (when-not (= selected-username
                   logged-in-username)
        (let [xhr (sjax
                    {:url rurls/get-chat-history-url
                     :entity [logged-in-username
                              selected-username]})
              response (get-response
                         xhr)]
          (reset!
            messages
            (:data response))
         ))
      @messages))
 )

(defn send-chat-message
  "Send message to selected user"
  [{load-chat-history :evt-fn
    logged-in-username :evt-p}
   element
   event]
  (when-let [selected-username-el (md/query-selector-on-element
                                    ".chat"
                                    "#chat-users")]
    (let [selected-username (.-innerHTML
                              (aget
                                (.-selectedOptions
                                  selected-username-el)
                                0))]
      (when (and (not= selected-username
                       logged-in-username)
                 (not= selected-username
                       (get-label 33))
             )
        (let [text-message-el (md/query-selector-on-element
                                ".chat"
                                "#chat-message-input")
              text-message (md/get-value
                             text-message-el)]
          (when-not (cstring/blank?
                      text-message)
            (let [xhr (sjax
                        {:url rurls/send-chat-message-url
                         :entity {:usernames [logged-in-username
                                              selected-username]
                                  :message {:username logged-in-username
                                            :text text-message}}
                         })
                  response (get-response
                             xhr)]
              (load-chat-history
                logged-in-username)
              (aset
                text-message-el
                "value"
                ""))
           ))
       ))
   ))

(defn send-chat-message-on-enter
  "Send chat message on enter"
  [evt-p
   element
   event]
  (when (= (.-keyCode
             event)
           13)
    (send-chat-message
      evt-p
      element
      event))
 )

