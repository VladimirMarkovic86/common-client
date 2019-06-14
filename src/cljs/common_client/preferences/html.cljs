(ns common-client.preferences.html
  (:require [htmlcss-lib.core :refer [gen div label input]]
            [language-lib.core :refer [get-label]]
            [framework-lib.core :as frm]
            [js-lib.core :as md]
            [clojure.string :as cstring]
            [common-middle.session :as ssn]
            [common-middle.language.entity :as cmle]
            [common-middle.user.entity :as cmue]
            [common-middle.role.entity :as cmre]
            [common-client.preferences.controller :as ccpc]))

(def is-called-read-preferences-a
     (atom false))

(defn display-as-selected-rows
  "Displays selected item in selection div"
  [evt-p
   element
   event]
  (let [div-container (.closest
                        element
                        ".dropdown-container.dropdown-container-rows")
        div-selection (md/query-selector-on-element
                        div-container
                        ".dropdown-selection.dropdown-selection-rows")
        selected-label (md/get-inner-html
                         element)
        selected-value (cstring/replace
                         selected-label
                         "x"
                         "")]
    (md/remove-element-content
      div-selection)
    (md/append-element
      div-selection
      (gen
        (div
          selected-label
          {:selected-value (str
                             selected-value)})
       ))
   ))

(defn generate-row-number-dropdown-options
  "Generates row number dropdown options"
  [selection]
  (let [options-vector [1 2 3 4 5 10 20 25 50 100]
        div-options-vector (atom [])]
    (doseq [option options-vector]
      (swap!
        div-options-vector
        conj
        (div
          (str
            "x" option)
          nil
          {:onclick {:evt-fn display-as-selected-rows
                     :evt-p {:new-card-columns nil
                             :new-table-rows option}}
           }))
     )
    (div
      [(div
         (case selection
           1 (div
               "x1"
               {:selected-value (str
                                  selection)})
           2 (div
               "x2"
               {:selected-value (str
                                  selection)})
           3 (div
               "x3"
               {:selected-value (str
                                  selection)})
           4 (div
               "x4"
               {:selected-value (str
                                  selection)})
           5 (div
               "x5"
               {:selected-value (str
                                  selection)})
           10 (div
                "x10"
                {:selected-value (str
                                   selection)})
           20 (div
                "x20"
                {:selected-value (str
                                   selection)})
           25 (div
                "x25"
                {:selected-value (str
                                   selection)})
           50 (div
                "x50"
                {:selected-value (str
                                   selection)})
           100 (div
                 "x100"
                 {:selected-value (str
                                    selection)})
           (div
             "x?"))
         {:class "dropdown-selection dropdown-selection-rows"})
       (div
         @div-options-vector
         {:class "dropdown-menu dropdown-menu-rows"})
       ]
      {:class "dropdown-container dropdown-container-rows"}))
 )

(defn display-as-selected-columns
  "Displays selected item in selection div"
  [evt-p
   element
   event]
  (let [selected-value (.getAttribute
                         element
                         "selected-value")
        div-container (.closest
                        element
                        ".dropdown-container.dropdown-container-columns")
        div-selection (md/query-selector-on-element
                        div-container
                        ".dropdown-selection.dropdown-selection-columns")]
    (md/remove-element-content
      div-selection)
    (md/append-element
      div-selection
      (gen
        (case selected-value
          "0" (frm/svg-table-icon)
          "1" (frm/svg-one-column-icon)
          "2" (frm/svg-two-column-icon)
          "3" (frm/svg-three-column-icon)
          "4" (frm/svg-four-column-icon)
          "5" (frm/svg-five-column-icon)
          (frm/svg-table-icon))
       ))
   ))

(defn generate-column-number-dropdown-options
  "Generates column number dropdown options"
  [selection]
  (div
    [(div
       (case selection
         0 (frm/svg-table-icon)
         1 (frm/svg-one-column-icon)
         2 (frm/svg-two-column-icon)
         3 (frm/svg-three-column-icon)
         4 (frm/svg-four-column-icon)
         5 (frm/svg-five-column-icon)
         (frm/svg-table-icon))
       {:class "dropdown-selection dropdown-selection-columns"})
     (div
       [(frm/svg-table-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        (frm/svg-one-column-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        (frm/svg-two-column-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        (frm/svg-three-column-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        (frm/svg-four-column-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        (frm/svg-five-column-icon
          {:onclick {:evt-fn display-as-selected-columns}})
        ]
       {:class "dropdown-menu dropdown-menu-columns"})
     ]
    {:class "dropdown-container dropdown-container-columns"}))

(def build-specific-display-tab-content-a-fn
     (atom nil))

(defn build-display-tab-content
  "Builds display tab content"
  []
  (let [display-parameters [(div
                              [(label
                                 (get-label 85))
                               (div
                                 (generate-column-number-dropdown-options
                                   @cmle/card-columns-a))
                               (div
                                 (generate-row-number-dropdown-options
                                   @cmle/table-rows-a))
                               ]
                              {:class "parameter"
                               :parameter-name "language-entity"})
                            (div
                              [(label
                                 (get-label 86))
                               (div
                                 (generate-column-number-dropdown-options
                                   @cmue/card-columns-a))
                               (div
                                 (generate-row-number-dropdown-options
                                   @cmue/table-rows-a))
                               ]
                              {:class "parameter"
                               :parameter-name "user-entity"})
                            (div
                              [(label
                                 (get-label 87))
                               (div
                                 (generate-column-number-dropdown-options
                                   @cmre/card-columns-a))
                               (div
                                 (generate-row-number-dropdown-options
                                   @cmre/table-rows-a))
                               ]
                              {:class "parameter"
                               :parameter-name "role-entity"})
                            ]
        specific-display-parameters (when (fn?
                                            @build-specific-display-tab-content-a-fn)
                                      (@build-specific-display-tab-content-a-fn))]
    (apply
      conj
      display-parameters
      specific-display-parameters))
 )

(defn build-language-select-item
  "Builds language select item as clojure maps"
  [img-class
   item-label
   item-value
   & [evt]]
  (div
    [(div
       nil
       {:class img-class})
     (div
       item-label)
     ]
    {:selected-value item-value}
    evt))

(defn switch-language
  "Switches language but doesn't save selection"
  [evt-p
   element
   event]
  (let [item-value (.getAttribute
                     element
                     "selected-value")
        selected-item (case item-value
                        "english" (build-language-select-item
                                    "us-flag-img"
                                    (get-label 25)
                                    item-value)
                        "serbian" (build-language-select-item
                                    "rs-flag-img"
                                    (get-label 26)
                                    item-value)
                        (build-language-select-item
                          "us-flag-img"
                          (get-label 25)
                          item-value))]
    (md/remove-element
      ".parameter .language-menu > div:nth-child(1)")
    (md/prepend-element
      ".parameter .language-menu"
      (gen
        selected-item))
   ))

(defn build-language-tab-content
  "Builds language tab content"
  []
  (div
    [(label
       (get-label 23))
     (div
       (div
         [(case @ssn/selected-language
            "english" (build-language-select-item
                        "us-flag-img"
                        (get-label 25)
                        @ssn/selected-language)
            "serbian" (build-language-select-item
                        "rs-flag-img"
                        (get-label 26)
                        @ssn/selected-language)
            (build-language-select-item
              "us-flag-img"
              (get-label 25)
              @ssn/selected-language))
          (div
            [(build-language-select-item
               "us-flag-img"
               (get-label 25)
               "english"
               {:onclick {:evt-fn switch-language}})
             (build-language-select-item
               "rs-flag-img"
               (get-label 26)
               "serbian"
               {:onclick {:evt-fn switch-language}})
             ]
            {:class "lang-items"})
          ]
         {:class "language-menu"}))
     ]
    {:class "parameter"
     :parameter-name "language"})
 )

(defn switch-tab
  "Switches tabs"
  [evt-p
   element
   event]
  (let [tab-content-index (.getAttribute
                            element
                            "tab-bar-index")
        tab-bar-elements (md/query-selector-all-on-element
                           ".tab-bar"
                           "div")
        tab-display-elements (md/query-selector-all-on-element
                               ".tab-display"
                               "div")
        tab-content-element (md/query-selector-on-element
                              ".tab-display"
                              (str
                                "div[tab-content-index='" tab-content-index "']"))]
    (doseq [tab-bar-element tab-bar-elements]
      (md/remove-class
        tab-bar-element
        "active-tab"))
    (md/add-class
      element
      "active-tab")
    (doseq [tab-display-element tab-display-elements]
      (md/remove-class
        tab-display-element
        "active-tab-content"))
    (md/add-class
      tab-content-element
      "active-tab-content"))
 )

(defn build-preferences-popup
  "Build preferences content"
  []
  (let [tab-bar-content (atom [])
        tab-display-content (atom [])]
    (swap!
      tab-bar-content
      conj
      (div
        (div
          (get-label 84)
          {:class "tab-name"})
        {:class "tab active-tab"
         :tab-bar-index "0"}
        {:onclick {:evt-fn switch-tab}})
      (div
        (div
          (get-label 23)
          {:class "tab-name"})
        {:class "tab"
         :tab-bar-index "1"}
        {:onclick {:evt-fn switch-tab}})
     )
    (swap!
      tab-display-content
      conj
      (div
        (build-display-tab-content)
        {:class "tab-content active-tab-content"
         :tab-content-index "0"})
      (div
        (build-language-tab-content)
        {:class "tab-content"
         :tab-content-index "1"})
     )
    (div
      [(div
         @tab-bar-content
         {:class "tab-bar"})
       (div
         @tab-display-content
         {:class "tab-display"})
       (div
         (input
           ""
           {:type "button"
            :value (get-label 1)
            :class "btn"}
           {:onclick {:evt-fn ccpc/popup-preferences-set}})
         {:class "tab-command"})
       ]
      {:class "tab-container"}))
 )

(defn display-preferences-popup
  "Opens preferences modal popup"
  []
  (when-not @is-called-read-preferences-a
    (ccpc/read-preferences)
    (reset!
      is-called-read-preferences-a
      true))
  (let [heading (get-label 83)
        content (build-preferences-popup)]
    (frm/popup-fn
      {:heading heading
       :content content}))
 )

