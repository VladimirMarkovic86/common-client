(ns common-client.login.controller
  (:require [ajax-lib.core :refer [ajax sjax get-response]]
            [js-lib.core :as md]
            [utils-lib.core :as utils]
            [framework-lib.core :as frm]
            [common-client.login.html :as lhtml]
            [common-client.sign-up.controller :as suc]
            [common-client.allowed-actions.controller :as aa]
            [common-middle.request-urls :as rurls]
            [language-lib.core :refer [cached-labels]]))

(def custom-menu
     (atom nil))

(def logout-fn
     (atom nil))

(def logout-success-fn
     (atom nil))

(defn remove-main
  "Remove main page from HTML document"
  []
  (md/remove-element-content
    ".body"))

(defn set-cookie
  "Set cookie in browser"
  [cookie-value]
  (aset
    js/document
    "cookie"
    cookie-value))

(defn destroy-session-cookie
  "Destroy session cookie"
  []
  (set-cookie
    (str "session=destroyed; "
         "expires=Thu, 01 Jan 1970 00:00:01 GMT; "))
  (set-cookie
    (str "long_session=destroyed; "
         "expires=Thu, 01 Jan 1970 00:00:01 GMT; "))
 )

(defn is-session-expired
  "Check if session cookie exists"
  []
  (let [cookies (aget
                  js/document
                  "cookie")]
    (= -1
       (.indexOf
         cookies
         "session"))
   ))

(defn is-login-displayed
  "Check if login form displayed"
  []
  (md/query-selector
    "table.login"))

(defn logout-on-session-expired
  "Log out if session expired"
  []
  (md/timeout
    #(do
       (when (and (not (is-login-displayed))
                  (is-session-expired))
         (.reload
           js/location))
       (when (not
               (and (is-login-displayed)
                    (is-session-expired))
              )
         (logout-on-session-expired))
      )
    1000))

(defn change-language-fn
  "Function that resets cached labels and reads labels for chosen language"
  [evt-p
   element
   event]
  (reset!
    cached-labels
    [])
  (let [xhr (sjax
              {:url rurls/set-language-url
               :entity evt-p})]
    (.reload
      js/location))
 )

(defn main-page
  "Open main page"
  [xhr]
  (aa/get-allowed-actions)
  (let [response (get-response xhr)
        username (:username response)
        language-name (:language-name response)]
    (md/append-element
      ".body"
      (lhtml/template
        @logout-fn
        username
        change-language-fn
        language-name
        @custom-menu))
   ))

(defn login-success
  "Login success"
  [xhr
   ajax-params]
  (reset!
    cached-labels
    [])
  (md/remove-element-content
    ".body")
  (logout-on-session-expired)
  (main-page
    xhr))

(defn login-error
  "Login error"
  [xhr]
  (let [response (get-response
                   xhr)
        email (md/get-by-id
                "txtEmailId")
        password (md/get-by-id
                   "pswLoginId")]
    (md/remove-class
      email
      "error")
    (md/remove-class
      password
      "error")
    (md/remove-class
      email
      "success")
    (md/remove-class
      password
      "success")
    (md/add-class
      email
      (:email response))
    (md/add-class
      password
      (:password response))
   ))

(defn read-login-form
  "Read data from login form"
  []
  (let [email (md/get-value
                "#txtEmailId")
        password (md/get-value
                   "#pswLoginId")
        remember-me (md/get-checked
                      "#chkRememberMeId")]
    {:email email
     :password (utils/encrypt-password
                 password)
     :remember-me remember-me}))

(defn redirect-to-login
  "Redirect to login page"
  []
  (md/append-element
    ".body"
    (lhtml/form
      {:onclick
        {:evt-fn ajax
         :evt-p {:url rurls/login-url
                 :success-fn login-success
                 :error-fn login-error
                 :entity read-login-form}}
       }
      {:onclick
         {:evt-fn suc/sign-up-evt-fn
          :evt-p
            {:cancel-fn @logout-success-fn
             :cancel-evt
               {:onclick
                 {:evt-fn @logout-success-fn}}
             }}
       }))
 )

(defn logout-success
  "Logout success function"
  [xhr]
  (remove-main)
  (destroy-session-cookie)
  (redirect-to-login))

(defn logout-error
  "Logout error function"
  [xhr]
  (let [response (get-response xhr)
        message (:message response)
        status (:status response)]
    (frm/popup-fn
      {:heading status
       :content message}))
 )

(defn logout
  "Logout"
  [& optional]
  (ajax
    {:url rurls/logout-url
     :success-fn logout-success
     :error-fn logout-error
     :entity {:user "Bye"}}))

