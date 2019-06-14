(ns common-client.preferences.controller
  (:require [ajax-lib.core :refer [sjax get-response]]
            [js-lib.core :as md]
            [common-middle.request-urls :as rurls]
            [common-middle.language.entity :as cmle]
            [common-middle.user.entity :as cmue]
            [common-middle.role.entity :as cmre]
            [common-middle.session :as ssn]
            [framework-lib.core :as frm]
            [cljs.reader :as reader]))

(def set-specific-preferences-a-fn
     (atom nil))

(def gather-specific-preferences-a-fn
     (atom nil))

(def popup-specific-preferences-set-a-fn
     (atom nil))

(def selected-language-refresh
     (atom false))

(defn set-preferences-fn
  "Sets values of preferences atoms"
  [preferences]
  (let [{{{table-rows-l :table-rows
           card-columns-l :card-columns} :language-entity
          {table-rows-u :table-rows
           card-columns-u :card-columns} :user-entity
          {table-rows-r :table-rows
           card-columns-r :card-columns} :role-entity} :display} preferences]
    (reset!
      cmle/table-rows-a
      (or table-rows-l
          10))
    (reset!
      cmle/card-columns-a
      (or card-columns-l
          0))
    (reset!
      cmue/table-rows-a
      (or table-rows-u
          10))
    (reset!
      cmue/card-columns-a
      (or card-columns-u
          0))
    (reset!
      cmre/table-rows-a
      (or table-rows-r
          10))
    (reset!
      cmre/card-columns-a
      (or card-columns-r
          0))
   ))

(defn read-preferences
  "Reads preferences from database"
  []
  (let [xhr (sjax
              {:url rurls/read-preferences-url})
        response (get-response
                   xhr)
        preferences (:preferences response)]
    (set-preferences-fn
      preferences)
    (when (fn?
            @set-specific-preferences-a-fn)
      (@set-specific-preferences-a-fn
        preferences))
   ))

(defn gather-preferences
  "Gathers preferences from common project"
  []
  {:language @ssn/selected-language
	  :language-name (case @ssn/selected-language
	                   "english" "English"
	                   "serbian" "Serbian"
	                   "English")
   :display {:language-entity {:table-rows @cmle/table-rows-a
                               :card-columns @cmle/card-columns-a}
             :user-entity {:table-rows @cmue/table-rows-a
                           :card-columns @cmue/card-columns-a}
             :role-entity {:table-rows @cmre/table-rows-a
                           :card-columns @cmre/card-columns-a}}
   })

(defn save-preferences
  "Saves preferences in database"
  []
  (let [preferences (gather-preferences)
        preferences (if (fn?
                          @gather-specific-preferences-a-fn)
                      (assoc
                        preferences
                        :specific
                        (@gather-specific-preferences-a-fn))
                      preferences)
        xhr (sjax
              {:url rurls/save-preferences-url
               :entity {:preferences preferences}})
        response (get-response
                   xhr)]
     
   ))

(defn generic-preferences-set
  "Generic function sets preferences"
  [property-name
   columns-a
   rows-a]
  (let [entity-p (md/query-selector-on-element
                   ".tab-display"
                   (str
                     "div[parameter-name='"
                     property-name
                     "']"))
        card-columns (md/query-selector-on-element
                       entity-p
                       ".dropdown-selection-columns svg")
        card-columns-number (reader/read-string
                              (.getAttribute
                                card-columns
                                "selected-value"))
        void (reset!
               columns-a
               card-columns-number)
        rows (md/query-selector-on-element
               entity-p
               ".dropdown-selection-rows div")
        rows-number (reader/read-string
                      (.getAttribute
                        rows
                        "selected-value"))
        void (reset!
               rows-a
               rows-number)]
    
   ))

(defn preferences-language-set
  "Sets language into session atom"
  []
  (let [selected-language-el (md/query-selector-on-element
                               ".tab-display"
                               "div[parameter-name='language'] .language-menu > div:nth-child(1)")
        selected-language (.getAttribute
                            selected-language-el
                            "selected-value")]
    (when (not= selected-language
                @ssn/selected-language)
      (reset!
        ssn/selected-language
        selected-language)
      (reset!
        selected-language-refresh
        true))
   ))

(defn popup-preferences-set
  "Sets preferences in atoms"
  [evt-p
   element
   event]
  (generic-preferences-set
    "language-entity"
    cmle/card-columns-a
    cmle/table-rows-a)
  (generic-preferences-set
    "user-entity"
    cmue/card-columns-a
    cmue/table-rows-a)
  (generic-preferences-set
    "role-entity"
    cmre/card-columns-a
    cmre/table-rows-a)
  (preferences-language-set)
  (when (fn?
          @popup-specific-preferences-set-a-fn)
    (@popup-specific-preferences-set-a-fn))
  (save-preferences)
  (if @selected-language-refresh
    (.reload
      js/location)
    (let [close-popup-btn (md/query-selector-on-element
                            ".popup-window"
                            ".close-btn")]
      (md/dispatch-event
        "click"
        close-popup-btn))
   ))

(reset!
  frm/read-preferences-a-fn
  read-preferences)

(reset!
  frm/save-preferences-a-fn
  save-preferences)

