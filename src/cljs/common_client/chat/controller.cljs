(ns common-client.chat.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [language-lib.core :refer [get-label]]
            [common-middle.request-urls :as rurls]
            [common-middle.ws-request-actions :as wsra]
            [common-middle.session :as cms]
            [htmlcss-lib.core :refer [gen div audio]]
            [js-lib.core :as md]
            [clojure.string :as cstring]
            [cljs.reader :as reader]
            [websocket-lib.core :refer [websocket]]))

(def websocket-obj-a
     (atom nil))

(def display-call-screen-a-fn
     (atom nil))

(def remove-call-screen-a-fn
     (atom nil))

(def start-streaming-a-fn
     (atom nil))

(def stop-streaming-a-fn
     (atom nil))

(def screen-type-calling
     "calling")

(def screen-type-answering
     "answering")

(def screen-type-connected
     "connected")

(defn get-selected-chat-contact
  "Returns value of selected option in user select html element"
  []
  (when-let [selected-username-el (md/query-selector-on-element
                                    ".chat"
                                    "#chat-users")]
    (let [selected-username (aget
                              (aget
                                (aget
                                  selected-username-el
                                  "selectedOptions")
                                0)
                              "value")]
      selected-username))
 )

(defn accept-call-fn
  "Accepts received call"
  []
  (@remove-call-screen-a-fn)
  (@display-call-screen-a-fn
    screen-type-connected)
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     (:username @cms/logged-in-user))
               (not= selected-username
                     "-1"))
      (let [message {:action wsra/call-accepted-action
                     :receiver selected-username
                     :sender (:username @cms/logged-in-user)}]
        (.send
          @websocket-obj-a
          (str
            message))
       ))
   )
  (@start-streaming-a-fn))

(defn hang-up-call-fn
  "Hang up on a call or reject answering"
  []
  (@stop-streaming-a-fn)
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     (:username @cms/logged-in-user))
               (not= selected-username
                     "-1"))
      (let [message {:action wsra/hang-up-call-action
                     :receiver selected-username
                     :sender (:username @cms/logged-in-user)}]
        (.send
          @websocket-obj-a
          (str
            message))
       ))
   )
  (@remove-call-screen-a-fn))

(defn display-call-screen-fn
  "Displays call screen"
  [screen-type]
  (let [call-screen-el (gen
                         (div
                           [(div
                              nil
                              {:class "call-background"})
                            (div
                              nil
                              {:class "user-image default-user-img"})
                            (div
                              (get-selected-chat-contact)
                              {:class "contact-name"})
                            (div
                              [(when (= screen-type
                                        screen-type-answering)
                                 (div
                                   (div
                                     nil
                                     {:class "phone-img"})
                                   {:class "phone-call"}
                                   {:onclick {:evt-fn accept-call-fn}}))
                               (div
                                 (div
                                   nil
                                   {:class "phone-img"})
                                 {:class "hang-up"}
                                 {:onclick {:evt-fn hang-up-call-fn}})
                               ]
                              {:class "phone-action"})
                            (div
                              (let [call-label (atom "")]
                                (when (= screen-type
                                         screen-type-calling)
                                  (reset!
                                    call-label
                                    (get-label 89))
                                 )
                                (when (= screen-type
                                         screen-type-answering)
                                  (reset!
                                    call-label
                                    (get-label 90))
                                 )
                                (when (= screen-type
                                         screen-type-connected)
                                  (reset!
                                    call-label
                                    (get-label 91))
                                 )
                                @call-label)
                              {:class "call-label"})
                            ]
                           {:class "call-modal"}))]
    (md/append-element
      "body"
      call-screen-el))
 )

(reset!
  display-call-screen-a-fn
  display-call-screen-fn)

(defn remove-call-screen-fn
  "Removes call screen"
  []
  (md/remove-element
    ".call-modal"))

(reset!
  remove-call-screen-a-fn
  remove-call-screen-fn)

(defn scroll-chat-to-bottom
  "Scrolls chat content to bottom"
  []
  (let [chat-history-el (md/query-selector-on-element
                          ".chat"
                          ".chat-history")]
    (aset
      chat-history-el
      "scrollTop"
      (aget
        chat-history-el
        "scrollTopMax"))
   ))

(defn get-chat-users
  "Get all users for chat"
  []
  (let [xhr (sjax
              {:url rurls/get-chat-users-url})
        response (get-response
                   xhr)
        users (:data response)
        users-reduced (reduce
                        (fn [acc
                             elem]
                          (let [username (:username elem)]
                            (if-not (= (:username @cms/logged-in-user)
                                       username)
                              (conj
                                acc
                                elem)
                              acc))
                         )
                        []
                        users)]
    users-reduced))

(defn send-audio-chunk-ws
  "Sends audio chunk through websocket"
  [audio-chunk]
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     (:username @cms/logged-in-user))
               (not= selected-username
                     "-1"))
      (when-not (cstring/blank?
                  audio-chunk)
        (let [message {:action wsra/send-audio-chunk-action
                       :receiver selected-username
                       :sender (:username @cms/logged-in-user)
                       :audio-chunk audio-chunk}]
          (.send
            @websocket-obj-a
            (str
              message))
         ))
     ))
  )

(defn make-a-call-ws
  "Makes call to selected contact"
  []
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     (:username @cms/logged-in-user))
               (not= selected-username
                     "-1"))
      (let [message {:action wsra/make-a-call-action
                     :receiver selected-username
                     :sender (:username @cms/logged-in-user)}]
        (display-call-screen-fn
          screen-type-calling)
        (.send
          @websocket-obj-a
          (str
            message))
       ))
   ))

(def media-recorder-a
     (atom nil))

(def recording-a
     (atom false))

(def is-playing-a
     (atom false))

(def audio-chunks-a
     (atom []))

(def current-index-a
     (atom 0))

(def chunk-duration
     (atom 200))

(defn play-chunks-fn
  "Play chunks"
  []
  (when @is-playing-a
    (let [audio-chunk-el (get
                           @audio-chunks-a
                           @current-index-a)]
      (if audio-chunk-el
        (let [audio-el (gen
                         (audio))]
          (aset
            audio-el
            "src"
            audio-chunk-el)
          (.play
            audio-el)
          (swap!
            current-index-a
            inc)
          (md/timeout
            #(play-chunks-fn)
            @chunk-duration))
        (md/timeout
          #(play-chunks-fn)
          @chunk-duration))
     ))
 )

(defn make-chunks-fn
  "Make chunks"
  []
  (when (and @recording-a
             (not
               (nil?
                 @media-recorder-a))
         )
    (when-not (= (aget
                   @media-recorder-a
                   "state")
                 "inactive")
      (.stop
        @media-recorder-a))
    (.start
      @media-recorder-a)
    (js/setTimeout
      #(make-chunks-fn)
      @chunk-duration))
  (when (and (not
               @recording-a)
             (not
               (nil?
                 @media-recorder-a))
         )
    (.stop
      @media-recorder-a)
    (reset!
      media-recorder-a
      nil))
 )

(defn initialize-media-recorder
  "Initialize media recorder"
  []
  (reset!
    media-recorder-a
    nil)
  (reset!
    recording-a
    true)
  (reset!
    audio-chunks-a
    [])
  (reset!
    current-index-a
    0)
  (let [media-devices (aget
                        js/navigator
                        "mediaDevices")
        selected-audio-input (md/get-value
                               "#chat-call-input")
        audio-document (js-obj
                         "audio"
                           (js-obj
                             "deviceId"
                               (js-obj
                                 "exact" selected-audio-input))
                        )
        audio-promise (.getUserMedia
                        media-devices
                        audio-document)]
    (.then
      audio-promise
      ((fn []
         (fn [stream]
           (reset!
             media-recorder-a
             (js/MediaRecorder.
               stream))
           (.addEventListener
             @media-recorder-a
             "dataavailable"
             ((fn []
                (fn [event]
                  (let [file-reader (js/FileReader.)
                        onload (aset
                                 file-reader
                                 "onloadend"
                                 ((fn []
                                    (fn [e]
                                      (send-audio-chunk-ws
                                        (aget
                                          (aget
                                            e
                                            "target")
                                          "result"))
                                     ))
                                  ))]
                    (.readAsDataURL
                      file-reader
                      (aget
                        event
                        "data"))
                   ))
               ))
            )
           (make-chunks-fn))
        ))
     ))
 )

(defn start-streaming-fn
  "Start streaming audio to selected username"
  []
  (initialize-media-recorder))

(reset!
  start-streaming-a-fn
  start-streaming-fn)

(defn stop-streaming-fn
  "Stop streaming audio to selected user"
  []
  (reset!
    recording-a
    false)
  (reset!
    is-playing-a
    false))

(reset!
  stop-streaming-a-fn
  stop-streaming-fn)

(defn generate-chat-history
  "Generate html of chat history"
  [chat-history]
  (let [messages (:messages chat-history)
        html-messages (atom [])]
    (doseq [{username :username
             text :text
             sent-at :sent-at} messages]
      (swap!
        html-messages
        conj
        (div
          (div
            text
            {:class (if (= username
                           (:username @cms/logged-in-user))
                      "my-message"
                      "contacts-message")})
          {:class (if (= username
                         (:username @cms/logged-in-user))
                    "my-message-container"
                    "contacts-message-container")})
       ))
    (gen
      (div
        @html-messages))
   ))

(defn get-chat-history
  "Get chat history for selected user and logged in user"
  []
  (let [selected-username (get-selected-chat-contact)]
    (when (not= selected-username
                (:username @cms/logged-in-user))
      (let [message {:action wsra/get-chat-history-action
                     :usernames [(:username @cms/logged-in-user)
                                 selected-username]}]
        (.send
          @websocket-obj-a
          (str
            message))
       ))
   ))

(defn selected-contact-username
  "On every contact username change read
   chat history and initialize media recorder"
  []
  (get-chat-history))

(defn send-chat-message-ws
  "Send chat message to contact"
  [evt-p
   element
   event]
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     (:username @cms/logged-in-user))
               (not= selected-username
                     "-1"))
      (let [text-message-el (md/query-selector-on-element
                              ".chat"
                              "#chat-message-input")
            text-message (md/get-value
                           text-message-el)]
        (when-not (cstring/blank?
                    text-message)
          (let [message {:action wsra/send-chat-message-action
                         :usernames [(:username @cms/logged-in-user)
                                     selected-username]
                         :message {:username (:username @cms/logged-in-user)
                                   :text text-message}}
                html-message-el (gen
                                  (div
                                    (div
                                      text-message
                                      {:class "my-message"})
                                    {:class "my-message-container"}))]
            (md/append-element
              ".chat-history"
              html-message-el)
            (scroll-chat-to-bottom)
            (.send
              @websocket-obj-a
              (str
                message))
            (aset
              text-message-el
              "value"
              ""))
         ))
     ))
 )

(defn send-chat-message-on-enter
  "Send chat message on enter"
  [evt-p
   element
   event]
  (when (= (aget
             event
             "keyCode")
           13)
    (send-chat-message-ws
      evt-p
      element
      event))
 )

(defn close-connection-ws-fn
  "Closes websocket connection with server"
  []
  (let [message {:action wsra/close-connection-action
                 :username (:username @cms/logged-in-user)}]
    (.send
      @websocket-obj-a
      (str
        message))
   ))

(defn close-connection-if-user-left-chat
  "Close connection if user left chat page"
  []
  (if (md/query-selector-on-element
        ".content"
        ".chat")
    (js/setTimeout
      #(close-connection-if-user-left-chat)
      1000)
    (close-connection-ws-fn))
 )

(defn establish-chat-connection-ws-fn
  "Onopen websocket event save reference to websocket object"
  [event]
  (let [websocket-obj (aget
                        event
                        "target")]
    (try
      (reset!
        websocket-obj-a
        websocket-obj)
      (let [message {:action wsra/establish-connection-action
                     :username (:username @cms/logged-in-user)}]
        (.send
          @websocket-obj-a
          (str
            message))
        (close-connection-if-user-left-chat))
      websocket-obj
      (catch js/Error e
        (.error
          js/console
          e))
     ))
 )

(defn chat-ws-onmessage-fn
  "Onmessage websocket event receive message"
  [event]
  (let [response (reader/read-string
                   (aget
                     event
                     "data"))
        action (:action response)]
    (when (= action
             wsra/receive-chat-message-action)
      (let [{username :username
             text :text} (:message response)]
        (when (= username
                 (get-selected-chat-contact))
          (let [html-message-el (gen
                                  (div
                                    (div
                                      text
                                      {:class "contacts-message"})
                                    {:class "contacts-message-container"}))]
            (md/append-element
              ".chat-history"
              html-message-el)
            (scroll-chat-to-bottom))
         ))
     )
    (when (= action
             wsra/receive-chat-history-action)
      (let [chat-history (:message response)
            chat-history-html (generate-chat-history
                                chat-history)]
        (md/remove-element-content
          ".chat-history")
        (md/append-element
          ".chat-history"
          chat-history-html)
        (scroll-chat-to-bottom))
     )
    (when (= action
             wsra/receive-audio-chunk-action)
      (let [{audio-chunk :audio-chunk} response]
        (if (< (count
                 @audio-chunks-a)
               200)
          (swap!
            audio-chunks-a
            conj
            audio-chunk)
          (do
            (reset!
              audio-chunks-a
              [])
            (reset!
              current-index-a
              0)
            (swap!
              audio-chunks-a
              conj
              audio-chunk))
         )
        (when-not @is-playing-a
          (reset!
            is-playing-a
            true)
          (play-chunks-fn))
       ))
    (when (= action
             wsra/receive-call-action)
      (let [sender-name (:sender response)]
        (if (= sender-name
               (get-selected-chat-contact))
          (do
            (remove-call-screen-fn)
            (display-call-screen-fn
              screen-type-answering))
          (when (and (not= sender-name
                           (:username @cms/logged-in-user))
                     (not= sender-name
                           "-1"))
            (let [message {:action wsra/unavailable-action
                           :receiver sender-name
                           :sender (:username @cms/logged-in-user)}]
              (.send
                @websocket-obj-a
                (str
                  message))
             ))
         ))
     )
    (when (= action
             wsra/call-accepted-action)
      (remove-call-screen-fn)
      (display-call-screen-fn
        screen-type-connected)
      (start-streaming-fn))
    (when (= action
             wsra/hang-up-call-action)
      (stop-streaming-fn)
      (remove-call-screen-fn))
    (when (= action
             wsra/unavailable-action)
      (remove-call-screen-fn))
   ))

(defn websocket-default-close
  "Default close of websocket"
  [event]
  (let [response (reader/read-string
                   (aget
                     event
                     "reason"))
        action (:action response)]
    (when (= action
             wsra/rejected-action)
      (let [status (:status response)
            message (:message response)]
        (.log
          js/console
          (str
            "status: "
            status))
        (.log
          js/console
          (str
            "message: "
            message))
       ))
   ))

(defn chat-ws-fn
  "Establish websocket connection with server"
  []
  (websocket
    rurls/chat-url
    {:onopen-fn establish-chat-connection-ws-fn
     :onmessage-fn chat-ws-onmessage-fn
     :onclose-fn websocket-default-close}))

