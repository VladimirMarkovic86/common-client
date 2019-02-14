(ns common-client.chat.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [common-middle.request-urls :as rurls]
            [common-middle.ws-request-actions :as wsra]
            [htmlcss-lib.core :refer [gen div audio]]
            [js-lib.core :as md]
            [clojure.string :as cstring]
            [cljs.reader :as reader]
            [websocket-lib.core :refer [websocket]]))

(def logged-in-username-a
     (atom nil))

(def websocket-obj-a
     (atom nil))

(defn scroll-chat-to-bottom
  "Scrolls chat content to bottom"
  []
  (let [chat-history-el (md/query-selector-on-element
                          ".chat"
                          ".chat-history")]
    (aset
      chat-history-el
      "scrollTop"
      (.-scrollTopMax
        chat-history-el))
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
                            (if-not (= @logged-in-username-a
                                       username)
                              (conj
                                acc
                                elem)
                              acc))
                         )
                        []
                        users)]
    users-reduced))

(defn get-selected-chat-contact
  "Returns value of selected option in user select html element"
  []
  (when-let [selected-username-el (md/query-selector-on-element
                                    ".chat"
                                    "#chat-users")]
    (let [selected-username (.-value
                              (aget
                                (.-selectedOptions
                                  selected-username-el)
                                0))]
      selected-username))
 )

(defn send-audio-chunk-ws
  "Sends audio chunk through websocket"
  [audio-chunk]
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     @logged-in-username-a)
               (not= selected-username
                     "-1"))
      (when-not (cstring/blank?
                  audio-chunk)
        (let [message {:action wsra/send-audio-chunk-action
                       :receiver selected-username
                       :sender @logged-in-username-a
                       :audio-chunk audio-chunk}]
          (.send
            @websocket-obj-a
            (str
              message))
         ))
     ))
  )

(def media-recorder-a
     (atom nil))

(def recording-a
     (atom false))

(def audio-el-a
     (atom
       (gen
         (audio))
      ))

(def audio-chunks-a
     (atom []))

(def current-index-a
     (atom 0))

(defn play-chunks-fn
  "Play chunks"
  []
  (let [audio-chunk-el (get
                         @audio-chunks-a
                         @current-index-a)]
    (when audio-chunk-el
      (aset
        @audio-el-a
        "src"
        audio-chunk-el)
      (.play
        @audio-el-a)
      (swap!
        current-index-a
        inc))
   ))

(defn initialize-media-recorder
  "Initialize media recorder"
  []
  (reset!
    media-recorder-a
    nil)
  (reset!
    recording-a
    false)
  (reset!
    audio-el-a
    (gen
      (audio
        ""
        nil
        {:onended {:evt-fn play-chunks-fn}}))
   )
  (reset!
    audio-chunks-a
    [])
  (reset!
    current-index-a
    0)
  (let [media-devices (.-mediaDevices
                        js/navigator)
        audio-document (js-obj
                         "audio" true)
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
                                        (.-result
                                          (.-target
                                            e))
                                       ))
                                   ))
                                )]
                    (.readAsDataURL
                      file-reader
                      (.-data
                        event))
                   ))
               ))
            ))
        ))
     ))
 )

(defn make-chunks-fn
  "Make chunks"
  []
  (when @recording-a
    (.stop
      @media-recorder-a)
    (.start
      @media-recorder-a)
    (js/setTimeout
      #(make-chunks-fn)
      1000))
  (when-not @recording-a
    (.stop
      @media-recorder-a))
 )

(defn start-streaming
  "Start streaming audio to selected username"
  []
  (reset!
    recording-a
    true)
  (make-chunks-fn))

(defn stop-streaming
  "Stop streaming audio to selected user"
  []
  (reset!
    recording-a
    false))

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
                           @logged-in-username-a)
                      "my-message"
                      "contacts-message")})
          {:class (if (= username
                         @logged-in-username-a)
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
                @logged-in-username-a)
      (let [message {:action wsra/get-chat-history-action
                     :usernames [@logged-in-username-a
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
  (get-chat-history)
  #_(initialize-media-recorder))

(defn send-chat-message-ws
  "Send chat message to contact"
  [evt-p
   element
   event]
  (let [selected-username (get-selected-chat-contact)]
    (when (and (not= selected-username
                     @logged-in-username-a)
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
                         :usernames [@logged-in-username-a
                                     selected-username]
                         :message {:username @logged-in-username-a
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
  (when (= (.-keyCode
             event)
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
                 :username @logged-in-username-a}]
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
  (let [websocket-obj (.-target
                        event)]
    (try
      (reset!
        websocket-obj-a
        websocket-obj)
      (let [message {:action wsra/establish-connection-action
                     :username @logged-in-username-a}]
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
                   (.-data
                     event))
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
      (let [{sender-username :sender
             audio-chunk :audio-chunk} response]
        (swap!
          audio-chunks-a
          conj
          audio-chunk)
        (play-chunks-fn))
     )
    (when (= action
             "incoming-call")
      ;Implement answer functionality
     ))
 )

(defn websocket-default-close
  "Default close of websocket"
  [event]
  (let [response (reader/read-string
                   (.-reason
                     event))
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

