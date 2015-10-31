(ns re-frogger.views
    (:require [re-frame.core :refer [subscribe dispatch dispatch-sync] :as re-frame]
              [domina :as d]
              [domina.events :as ev]
              [domina.css :as css]
              [reagent.core :as reagent]
              [cljs.core.async :refer [<! chan put! timeout pub sub]])
    (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(def ctg
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn score
  "Shows the current score."
  []
  (let [score (subscribe [:score])]
    (fn []
      [:h2 "score: " @score])))

(defn time-board
  "Shows the elapsed time in seconds."
  []
  (let [frame (subscribe [:frame])]
    [:h2 "time:" (int (/ @frame 3))]))

(defn tile 
  "Basic tile component."
  [index]
  [:div {:class "tile"}])

(defn lane 
  "A single lane component."
  []
  [:div {:class "lane"}
   (for [i (range 15)]
    ^{:key i} [tile i])])

(defn lanes 
  "Lanes component."
  [lanes]
  [:div
   (for [i (range 11)]
     ^{:key i} [lane])])

(defn player 
  "The component representing the player.
  It gets its position from the subscription to :position."
  []
  (let [position (subscribe [:position])]
    [:div {:id "player"
           :style {:top (* (:y @position) 22)
                   :left (* (:x @position) 22)}}]))

(defn- bind-keys 
  "Handles key presses."
  [_]
  (ev/listen! (css/sel "body") :keypress #(dispatch [:handle-keyboard (:keyCode %)])))

(defn- unbind-keys 
  "Removes the listener on component unmount."
  [_]
  (ev/unlisten! (css/sel "body") :keypress))

(defn game-over 
  "A component that shows the game over message.
  It's subscribed to :game-over."
  []
  (let [game-over (subscribe [:game-over])]
    (if @game-over 
      [:h3 {:class "gameover"} 
                   "GAME-OVER - PRESS R TO RESTART"])))

(defn car 
  "A dumb car component."
  [car]
  [:div {:class (str "car " "model" (:s car))
         :style {:top (* (:y car) 22)
                 :left (* (:x car) 22)}}])

(defn traffic 
  "A component that paints cars.
  It's subscribed to :cars."
  []
  (let [cars (subscribe [:cars])]
    [:div {:class "traffic"}
     (map-indexed (fn [i c] ^{:key i} [car c]) @cars)]))

(defn world 
  "The world is subscribed to :frame and it updates various things."
  []
  (let [frame (subscribe [:frame])]
    (when @frame
      (do
        (dispatch [:update-position])
        (dispatch [:update-traffic])
        (dispatch [:generate-cars])
        (dispatch [:check-collisions])))))

(defn board 
  "The board is a form-3 component, as it needs to call the bind-keys
  and unbind-keys functions in its life-cycle."
  []
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
                              [:div
                               [ctg {:transitionName "gameover"}
                                ^{:key "playa"} [player]]]                        
                              [lanes @lane-types]])})))
    
(defn time-loop 
  "A loop that updates the :frame in db.
  There's a check on :running to avoid multiple time-loops running."
  []
  (let [running (subscribe [:running])]
    (if-not @running
      (do (dispatch [:start-game])
          (go-loop [game-over (subscribe [:game-over])]
            (<! (timeout 333))
            (dispatch [:frame])
            (recur game-over))))))

(defn main-panel 
  "The main component."
  []
  (let [name (subscribe [:name])]
    (fn []
      (time-loop)
      [:div
       [:h1 @name]
       [score]
       [time-board]
       [board]])))

