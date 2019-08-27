(ns common-client.chat.view
  (:require [htmlcss-lib.core :refer [gen div select option input]]
            [js-lib.core :as md]
            [common-client.chat.controller :as cc]
            [language-lib.core :refer [get-label]]))

(defn fill-chat-call-input-select
  "Fills up chat call input select with available system audio inputs"
  []
  (let [media-devices (aget
                        js/navigator
                        "mediaDevices")
        enumerate-devices-promise (.enumerateDevices
                                    media-devices)]
    (.then
      enumerate-devices-promise
      ((fn []
         (fn [device-infos]
           (doseq [device-info device-infos]
             (let [device-info-kind (aget
                                      device-info
                                      "kind")
                   audio-select (md/query-selector
                                  "#chat-call-input")]
               (when (= device-info-kind
                        "audioinput")
                 (let [device-label (aget
                                      device-info
                                      "label")
                       option-label (if (or (nil?
                                              device-label)
                                            (empty?
                                              device-label))
                                      (str
                                        "microphone "
                                        (inc
                                          (aget
                                            audio-select
                                            "length"))
                                       )
                                      device-label)
                       option (gen
                                (option
                                  option-label
                                  {:value (aget
                                            device-info
                                            "deviceId")})
                               )]
                   (md/append-element
                     audio-select
                     option))
                ))
            ))
        ))
   ))
 )

(defn chat-pure-html
  "Construct html chat view and append it"
  []
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
                            {:onclick {:evt-fn cc/send-chat-message-ws}})
                          ]
                         {:class "chat-message"})
                       (div
                         [(select
                            nil
                            {:id "chat-call-input"})
                          (input
                            ""
                            {:class "btn"
                             :type "button"
                             :value (get-label 88)}
                            {:onclick {:evt-fn cc/make-a-call-ws}})
                          ]
                         {:class "chat-call"})
                       ]
                      {:class "chat"}))]
    (md/append-element
      ".content"
      chat-form)
    (fill-chat-call-input-select)
    (cc/chat-ws-fn))
 )

