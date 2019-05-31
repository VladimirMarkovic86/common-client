(ns common-client.login.controller
  (:require [ajax-lib.core :refer [ajax sjax get-response]]
            [js-lib.core :as md]
            [utils-lib.core :as utils]
            [framework-lib.core :as frm]
            [validator-lib.core :refer [validate-field]]
            [common-client.login.html :as lhtml]
            [common-client.sign-up.controller :as suc]
            [common-client.forgot-password.controller :as fpc]
            [common-client.allowed-actions.controller :as aa]
            [common-middle.request-urls :as rurls]
            [common-middle.session :as cms]
            [language-lib.core :refer [cached-labels get-label]]))

(def custom-menu
     (atom nil))

(def home-page-content
     (atom nil))

(def logout-fn
     (atom nil))

(def logout-success-fn
     (atom nil))

(def logged-in-user
     (atom nil))

(defn remove-main
  "Remove main page from HTML document"
  []
  (md/remove-element-content
    "body > div:first-child"))

(defn set-cookie
  "Set cookie in browser"
  [cookie-value]
  (aset
    js/document
    "cookie"
    cookie-value))

(defn is-session-expired
  "Check if session cookie exists"
  []
  (let [cookies (.-cookie
                  js/document)]
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
        language-changed-to (:language response)
        language-name (atom "")
        language-icon (atom "")]
    (reset!
      logged-in-user
      {:username username})
    (when (= language-changed-to
             "english")
      (reset!
        cms/selected-language
        language-changed-to)
      (reset!
        language-name
        (get-label 25))
      (swap!
        language-icon
        str
        "us-flag-img"))
    (when (= language-changed-to
             "serbian")
      (reset!
        cms/selected-language
        language-changed-to)
      (reset!
        language-name
        (get-label 26))
      (swap!
        language-icon
        str
        "rs-flag-img"))
    (md/append-element
      "body > div:first-child"
      (lhtml/template
        @logout-fn
        username
        change-language-fn
        @language-name
        @language-icon
        @custom-menu
        @home-page-content
        (:username @logged-in-user)))
   ))

(defn login-success
  "Login success"
  [xhr
   ajax-params]
  (reset!
    cached-labels
    [])
  (md/remove-element-content
    "body > div:first-child")
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
                   "pswLoginId")
        is-valid (atom true)]
    (validate-field
      email
      is-valid
      (get-label 58)
      (= (:email response)
         "error"))
    (validate-field
      password
      is-valid
      (get-label 59)
      (= (:password response)
         "error"))
   ))

(defn submit-form
  "Submit login form"
  [evt-p
   element
   event]
  (let [email (md/query-selector-on-element
                ".login"
                "#txtEmailId")
        password (md/query-selector-on-element
                   ".login"
                   "#pswLoginId")
        remember-me (md/query-selector-on-element
                      ".login"
                      "#chkRememberMeId")
        is-valid (atom true)]
    (validate-field
      email
      is-valid)
    (validate-field
      password
      is-valid)
    (when @is-valid
      (let [email (md/get-value
                    "#txtEmailId")
            password (md/get-value
                       "#pswLoginId")
            remember-me (md/get-checked
                          "#chkRememberMeId")]
        (ajax
          {:url rurls/login-url
           :success-fn login-success
           :error-fn login-error
           :entity {:email email
                    :password (utils/sha256
                                password)
                    :remember-me remember-me}}))
     ))
 )

(defn redirect-to-login
  "Redirect to login page"
  []
  (md/append-element
    "body > div:first-child"
    (lhtml/form-fn
      {:onclick
        {:evt-fn submit-form}}
      {:onclick
         {:evt-fn suc/sign-up-evt-fn
          :evt-p
            {:cancel-fn @logout-success-fn
             :cancel-evt
               {:onclick
                 {:evt-fn @logout-success-fn}}
             }}
       }
      {:onclick
         {:evt-fn fpc/forgot-password-evt-fn
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

