(ns re-frogger.views
    (:require [re-frame.core :refer [subscribe dispatch dispatch-sync] :as re-frame]
              [domina :as d]
              [domina.events :as ev]
              [domina.css :as css]
              [reagent.core :as reagent]
              [cljs.core.async :refer [<! chan put! timeout pub sub]])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(defn score []
  (let [score (subscribe [:score])]
    (fn []
      [:h2 "score: " @score])))

(defn frame []
  (let [frame (subscribe [:frame])]
    [:h2 "time:" (int (/ @frame 3))]))

(defn game-over []
  (let [game-over (subscribe [:game-over])]
    (when @game-over
      [:button {:on-click #(dispatch-sync [:initialize-db])} "GAME OVER - PRESS TO RESTART"])))

(defn tile [index]
  [:div {:class "tile"}])

(defn lane [lane-type]
  [:div {:class (str "lane " "type" lane-type)}
   (for [i (range 15)]
    ^{:key i} [tile i])])


(defn player []
  (let [position (subscribe [:position])]
    [:div {:id "player"
           :style {:top (* (:y @position) 22)
                   :left (* (:x @position) 22)}}]))

(defn- bind-keys [_]
  (ev/listen! (css/sel "body") :keypress #(dispatch [:move-player (:keyCode %)])))

(defn- unbind-keys [_]
  (ev/unlisten! (css/sel "body") :keypress))


(defn board []
  (let [lane-types (subscribe [:lane-types])]
    (reagent/create-class {:component-did-mount bind-keys
                           :component-will-unmount unbind-keys
                           :display-name "board"
                           :reagent-render 
                           (fn []
                             [:div {:class "board"}
                              [player]
                              (for [lane-type @lane-types]
                                [lane lane-type])])})))


(defn time-loop []
  (go-loop [game-over (subscribe [:game-over])]
    (<! (timeout 333))
    (when-not @game-over
      (dispatch [:frame]))
    (recur game-over)))

(defn main-panel []
  (let [name (subscribe [:name])]
    (fn []
      (time-loop)
      [:div
       [:h1 @name]
       [score]
       [frame]
       [board]
       [game-over]])))

