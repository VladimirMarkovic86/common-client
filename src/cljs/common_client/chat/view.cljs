(ns common-client.chat.view
  (:require [htmlcss-lib.core :refer [gen div select option input]]
            [js-lib.core :as md]
            [common-client.chat.controller :as cc]
            [language-lib.core :refer [get-label]]))

(defn chat-pure-html
  "Construct html chat view and append it"
  [logged-in-username]
  (reset!
    cc/logged-in-username-a
    logged-in-username)
  (md/remove-element-content
    ".content")
  (let [users (cc/get-chat-users)
        chat-form (gen
                    (div
                      [(div
                         [(select
                            (let [options (atom
                                            [(option
                                               (get-label 33)
                                               {:value "-1"})])]
                              (doseq [{username :username} users]
                                (swap!
                                  options
                                  conj
                                  (option
                                    username
                                    {:value username}))
                               )
                              @options)
                            {:id "chat-users"}
                            {:onchange {:evt-fn cc/selected-contact-username}})
                          (input
                            ""
                            {:id "chat-refresh"
                             :class "btn"
                             :value (get-label 69)
                             :type "button"}
                            {:onclick {:evt-fn cc/get-chat-history}})]
                         {:class "chat-commands"})
                       (div
                         ""
                         {:class "chat-history"})
                       (div
                         [(input
                            ""
                            {:id "chat-message-input"
                             :type "text"
                             :placeholder (get-label 66)}
                            {:onkeyup {:evt-fn cc/send-chat-message-on-enter}})
                          (input
                            ""
                            {:class "btn"
                             :type "button"
                             :value (get-label 67)}
                            {:onclick {:evt-fn cc/send-chat-message-ws}})]
                         {:class "chat-message"})
                       #_(div
                         [(input
                            ""
                            {:class "btn"
                             :type "button"
                             :value "Start streaming audio"}
                            {:onclick {:evt-fn cc/start-streaming}})
                          (input
                            ""
                            {:class "btn"
                             :type "button"
                             :value "Stop streaming audio"}
                            {:onclick {:evt-fn cc/stop-streaming}})
                          ]
                         {:class "audio-message"})
                       ]
                      {:class "chat"}))]
    (md/append-element
      ".content"
      chat-form)
    (cc/chat-ws-fn))
 )

