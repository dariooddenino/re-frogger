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

(defn tile [index]
  [:div {:class "tile"}])

(defn lane []
  [:div {:class "lane"}
   (for [i (range 15)]
    ^{:key i} [tile i])])

(defn lanes [lanes]
  [:div
   (for [i (range 11)]
     ^{:key i} [lane])])


(defn player []
  (let [position (subscribe [:position])]
    [:div {:id "player"
           :style {:top (* (:y @position) 22)
                   :left (* (:x @position) 22)}}]))

(defn- bind-keys [_]
  (ev/listen! (css/sel "body") :keypress #(dispatch [:handle-keyboard (:keyCode %)])))

(defn- unbind-keys [_]
  (ev/unlisten! (css/sel "body") :keypress))

(defn game-over []
  (let [game-over (subscribe [:game-over])]
    (if @game-over
      [:h3 {:class "gameover"} 
       "GAME-OVER - PRESS R TO RESTART"])))

(defn car [car]
  [:div {:class (str "car " "model" (:s car))
         :style {:top (* (:y car) 22)
                 :left (* (:x car) 22)}}])

(defn traffic []
  (let [cars (subscribe [:cars])
        real-cars @cars] ;; lazy reactive blah blah why? @TODO!
  [:div {:class "traffic"}
     (for [i (range (count real-cars))]
       ^{:key i} [car (nth real-cars i)])]))

(defn world []
  (let [frame (subscribe [:frame])]
    (when @frame
      (do
        (dispatch [:update-position])
        (dispatch [:update-traffic])
        (dispatch [:generate-cars])
        (dispatch [:check-collisions])))))

(defn board []
  (let [lane-types (subscribe [:lane-types])]
    (reagent/create-class {:component-did-mount bind-keys
                           :component-will-unmount unbind-keys
                           :display-name "board"
                           :reagent-render 
                           (fn []
                             [:div {:class "board"}
                              [world]                   
                              [game-over]
                              [traffic]
                              [player]                              
                              [lanes @lane-types]])})))
    
(defn time-loop []
  (let [running (subscribe [:running])]
    (if-not @running
      (do (dispatch [:start-game])
          (go-loop [game-over (subscribe [:game-over])]
            (<! (timeout 333))
            (dispatch [:frame])
            (recur game-over))))))

(defn main-panel []
  (let [name (subscribe [:name])]
    (fn []
      (time-loop)
      [:div
       [:h1 @name]
       [score]
       [frame]
       [board]])))

