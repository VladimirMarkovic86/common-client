(ns common-client.chat.view
  (:require [htmlcss-lib.core :refer [gen div select option input]]
            [js-lib.core :as md]
            [common-client.chat.controller :as cc]
            [language-lib.core :refer [get-label]]))

(defn generate-chat-history
  "Generate html of chat history"
  [chat-history
   logged-in-username]
  (let [messages (atom [])]
    (doseq [{username :username
             text :text
             sent-at :sent-at} chat-history]
      (swap!
        messages
        conj
        (div
          (div
            text
            {:class (if (= username
                           logged-in-username)
                      "my-message"
                      "contacts-message")})
          {:class (if (= username
                         logged-in-username)
                    "my-message-container"
                    "contacts-message-container")})
       ))
    (gen
      @messages))
 )

(def refresh-on
     (atom false))

(def is-first-time
     (atom true))

(defn load-chat-history
  "Load history in view"
  [logged-in-username
   & [element
      event
      recur-me]]
  (let [chat-history-el (md/query-selector-on-element
                          ".chat"
                          ".chat-history")
        scroll-value (atom nil)]
    (if chat-history-el
      (do
        (when-not (empty?
                    (.-innerHTML
                      chat-history-el))
          (let [scroll-top (.-scrollTop
                             chat-history-el)
                scroll-top-max (.-scrollTopMax
                                 chat-history-el)]
            (when-not (= scroll-top
                         scroll-top-max)
              (reset!
                scroll-value
                (.-scrollTop
                  chat-history-el))
             ))
         )
        (let [chat-history (cc/get-chat-history
                             logged-in-username)
              generated-chat-history (generate-chat-history
                                       chat-history
                                       logged-in-username)
              generated-chat-history-in-div (gen
                                              (div
                                                generated-chat-history))]
          (when-not (= (.-innerHTML
                         generated-chat-history-in-div)
                       (let [chat-history (md/query-selector-on-element
                                            ".chat"
                                            ".chat-history")
                             chat-history-inner-html (.-innerHTML
                                                       chat-history)]
                         chat-history-inner-html))
            (md/remove-element-content
              ".chat-history")
            (md/append-element
              ".chat-history"
              generated-chat-history))
         )
        (let [chat-history-el (md/query-selector-on-element
                                ".chat"
                                ".chat-history")]
          (if @scroll-value
            (aset
              chat-history-el
              "scrollTop"
              @scroll-value)
            (aset
              chat-history-el
              "scrollTop"
              (.-scrollTopMax
                chat-history-el))
           ))
        (when-not @refresh-on
          (reset!
            refresh-on
            true))
        (when (and @refresh-on
                   (or @is-first-time
                       recur-me))
          (js/setTimeout
            #(load-chat-history
               logged-in-username
               nil
               nil
               true)
            1000))
        (when @is-first-time
          (reset!
            is-first-time
            false))
       )
      (do
        (reset!
          refresh-on
          false)
        (reset!
          is-first-time
          true))
     ))
 )

(defn chat-pure-html
  "Construct html chat view and append it"
  [logged-in-username]
  (md/remove-element-content
    ".content")
  (let [users (cc/get-chat-users
                logged-in-username)
        options (atom
                  [(option
                     (get-label 33))])
        chat-form (gen
                    (div
                      [(div
                         [(select
                            (do
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
                            {:onchange {:evt-fn load-chat-history
                                        :evt-p logged-in-username}})
                          (input
                            ""
                            {:id "chat-refresh"
                             :class "btn"
                             :value (get-label 69)
                             :type "button"}
                            {:onclick {:evt-fn load-chat-history
                                       :evt-p logged-in-username}})]
                         {:class "chat-commands"})
                       (div
                         (let [chat-history (cc/get-chat-history
                                              logged-in-username)]
                           (generate-chat-history
                             chat-history
                             logged-in-username))
                         {:class "chat-history"})
                       (div
                         [(input
                            ""
                            {:id "chat-message-input"
                             :type "text"
                             :placeholder (get-label 66)}
                            {:onkeyup {:evt-fn cc/send-chat-message-on-enter
                                       :evt-p {:evt-fn load-chat-history
                                               :evt-p logged-in-username}}
                             })
                          (input
                            ""
                            {:class "btn"
                             :type "button"
                             :value (get-label 67)}
                            {:onclick {:evt-fn cc/send-chat-message
                                       :evt-p {:evt-fn load-chat-history
                                               :evt-p logged-in-username}}
                             })]
                         {:class "chat-message"})]
                      {:class "chat"}))]
    (md/append-element
      ".content"
      chat-form))
 )

