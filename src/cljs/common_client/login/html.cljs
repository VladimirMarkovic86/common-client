(ns common-client.login.html
  (:require [htmlcss-lib.core :refer [gen label input div nav header
                                      footer section aside fieldset
                                      form h2 p span a svg polyline]]
            [js-lib.core :as md]
            [framework-lib.core :refer [popup-fn]]
            [framework-lib.side-bar-menu :as sbm]
            [validator-lib.core :refer [validate-input]]
            [common-client.user.html :as uh]
            [common-client.role.html :as rh]
            [common-client.language.html :as lh]
            [common-client.chat.html :as ch]
            [common-client.preferences.html :as ccph]
            [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.functionalities :as fns]))

(def display-forgot-password-a
     (atom true))

(defn form-fn
  "Generate table HTML element that contains login form"
  [login-evt
   sign-up-evt
   forgot-password-evt]
  (gen
    (form
      (div
        [(fieldset
           [(label
              [(get-label 14)
               (input
                 ""
                 {:id "txtEmailId"
                  :type "text"
                  :placeholder (get-label 14)
                  :title (get-label 14)
                  :required true}
                 {:oninput {:evt-fn validate-input}})
               (span)])
            (label
              [(get-label 15)
               (input
                 ""
                 {:id "pswLoginId"
                  :type "password"
                  :placeholder (get-label 15)
                  :title (get-label 15)
                  :required true}
                 {:oninput {:evt-fn validate-input}})
               (span)])
            (label
              [(get-label 16)
               (input
                 ""
                 {:id "chkRememberMeId"
                  :type "checkbox"
                  :title (get-label 16)})
               (span)])
            (when @display-forgot-password-a
              (div
                (label
                  (a
                    (get-label 76)
                    {:id "forgotPasswordId"
                     :title (get-label 76)}
                    forgot-password-evt))
                {:style {:margin-bottom "27px"}}))
            ])
         (div
           [(input
              ""
              {:type "submit"
               :value (get-label 17)
               :class "btn btn-default"}
              login-evt)
            (input
              ""
              {:type "button"
               :value (get-label 18)
               :class "btn"}
              sign-up-evt)])]
       )
     {:class "login"
      :onsubmit "return false"
      :novalidate true
      :autocomplete "off"}))
 )

(defn home-fn
  "Generate and render home page"
  [{evt-p :evt-p
    collapse :collapse}
   & [element
      event]]
  (md/remove-element-content
    ".content")
  (when collapse
    (sbm/collapse-all-items))
  (md/append-element
    ".content"
    (gen
      (if evt-p
        evt-p
        [(h2
           "Home page")
         (p
           "Web app description")
         (input
           ""
           {:value "Popup"
            :type "button"}
           {:onclick {:evt-fn popup-fn
                      :evt-p {:content "Test content"
                              :heading "Test heading"}}
            })])
     ))
 )

(defn account-fn
  "Header navigation menu"
  [logout-fn
   username]
  [(div
     [(div
        ""
        {:class "default-user-img"})
      (div
        username)])
   (div
     [(div
        [(div
           ""
           {:class "preferences-img"})
         (div
           (get-label 83))]
        nil
        {:onclick {:evt-fn ccph/display-preferences-popup}})
      (div
        [(div
           ""
           {:class "logout-img"})
         (div
           (get-label 2))]
        nil
        {:onclick {:evt-fn logout-fn}})]
     {:class "account-items"})])

(defn language-fn
  "Language select language"
  [change-language-fn
   language-name
   language-icon]
  [(div
     [(div
        ""
        {:class language-icon})
      (div
        language-name)])
   (div
     [(div
        [(div
           ""
           {:class "us-flag-img"})
         (div
           (get-label 25))]
        nil
        {:onclick {:evt-fn change-language-fn
                   :evt-p {:language "english"
                           :language-name (get-label 25)}}
         })
      (div
        [(div
           ""
           {:class "rs-flag-img"})
         (div
           (get-label 26))]
        nil
        {:onclick {:evt-fn change-language-fn
                   :evt-p {:language "serbian"
                           :language-name (get-label 26)}}
         })]
     {:class "lang-items"})])

(defn side-bar-menu
  "Generate side bar menu vector"
  [custom-menu
   logged-in-username]
  (apply
    conj
    custom-menu
    [(ch/nav
       logged-in-username)
     (when (or (contains?
                 @allowed-actions
                 fns/user-create)
               (contains?
                 @allowed-actions
                 fns/user-read)
               (contains?
                 @allowed-actions
                 fns/role-create)
               (contains?
                 @allowed-actions
                 fns/role-read)
               (contains?
                 @allowed-actions
                 fns/language-create)
               (contains?
                 @allowed-actions
                 fns/language-read))
       {:label (get-label 32)
        :id "administration-nav-id"
        :sub-menu [(uh/nav)
                   (rh/nav)
                   (lh/nav)]})]
   ))

(defn render-svg-logo
  "Renders logo with svg element"
  [width
   height]
  (let [points-a (atom "")
        width-segments (range
                         0
                         (inc
                           width)
                         (int
                           (/ width
                              4))
                        )
        itr (atom 0)]
    (doseq [x width-segments]
      (swap!
        points-a
        str
        x
        ","
        (if (odd?
              @itr)
          0
          height)
        " ")
      (swap!
        itr
        inc))
    (svg
      (polyline
        ""
        {:class "logo-svg-polyline"
         :points @points-a})
      {:class "logo-svg"
       :width (str
                width "px")
       :height (str
                 height "px")})
   ))

(defn template
  "Template of main page"
  [logout-fn
   username
   change-language-fn
   language-name
   language-icon
   custom-menu
   home-page-content
   logged-in-username]
  (let [custom-menu (if (fn? custom-menu)
                      (custom-menu)
                      [])
        side-bar-menu-content (sbm/final-menu
                                (side-bar-menu
                                  custom-menu
                                  logged-in-username)
                                home-fn
                                home-page-content)]
    (gen
      [(header
         [(div
            (render-svg-logo
              60
              28)
            {:class "logo"}
            {:onclick {:evt-fn home-fn
                       :evt-p {:evt-p home-page-content
                               :collapse true}}
             })
          (div
            (account-fn
              logout-fn
              username)
            {:class "account-menu"})
          (div
            (language-fn
              change-language-fn
              language-name
              language-icon)
            {:class "language-menu"})])
       (aside
         (nav
           side-bar-menu-content))
       (section
         home-page-content
         {:class "content"})
       (footer
         "")])
   ))

