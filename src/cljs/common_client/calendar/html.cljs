(ns common-client.calendar.html
  (:require [language-lib.core :refer [get-label]]
            [common-client.allowed-actions.controller :refer [allowed-actions]]
            [common-middle.functionalities :as fns]
            [js-lib.core :as md]
            [htmlcss-lib.core :refer [gen div input]]
            [svg-lib.calendar.core :as svgc]
            [svg-lib.calendar.week :as svgw]
            [svg-lib.calendar.day :as svgd]))

(defn calendar-pure-html
  "Construct html calendar view and append it"
  []
  (md/remove-element-content
    ".content")
  (md/append-element
    ".content"
    (gen
      (div
        [(div
           [(div
              (input
                nil
                {:value (get-label
                          35)
                 :type "button"
                 :class "btn"}
                {:onclick {:evt-fn svgc/switch-to-previous}}))
            (div
              nil
              {:class "month-name"})
            (div
              (input
                nil
                {:value (get-label
                          36)
                 :type "button"
                 :class "btn"}
                {:onclick {:evt-fn svgc/switch-to-next}}))]
           {:class "calendar-commands"})
         (div
           nil
           {:class "calendar-container"})]
        {:class "calendar"}))
   )
  (svgc/draw-by-date
    (js/Date.))
 )

(defn nav
  "Returns map of menu item and it's sub items"
  []
  (when (some
          #{fns/item-create
            fns/item-read
            fns/item-update
            fns/item-delete}
          @allowed-actions)
    {:label (get-label
              92)
     :id "calendar-nav-id"
     :evt-fn calendar-pure-html}))

