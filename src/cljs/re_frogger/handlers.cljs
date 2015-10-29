(ns re-frogger.handlers
    (:require [re-frame.core :refer [register-handler dispatch] :as re-frame]
              [re-frogger.db :as db]))

(register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(def UP 119)
(def RIGHT 100)
(def DOWN 115)
(def LEFT 97)

(defn- new-position [position move]
  (condp = move
    UP (assoc position :y (dec (:y position)))
    DOWN (assoc position :y (inc (:y position)))
    RIGHT (assoc position :x (inc (:x position)))
    LEFT (assoc position :x (dec (:x position)))
    position))

(defn- valid-position? [position]
  (and (< (:x position) 15)
       (< (:y position) 11)
       (>= (:x position) 0)
       (>= (:y position) 0)))

(defn- move-player [position move]
  (let [new-pos (new-position position move)]
    (if (valid-position? new-pos)
      new-pos
      position)))

(register-handler
 :move-player
 (fn [db [_ move]]
   (let [position (move-player (-> db :game-state :position) move)
         game-over (-> db :game-state :game-over)]
     (if-not game-over
       (do (dispatch [:update-score (:y position)])
           (assoc-in db [:game-state :position] position))
       db))))

(register-handler
 :frame
 (fn [db [_ _]]
   (let [frame (-> db :game-state :frame)]
     (assoc-in db [:game-state :frame] (inc frame)))))

(register-handler
 :check-game-over
 (fn [db [_ score]]
   (if (= score 10)
     (assoc-in db [:game-state :game-over] true)
     db)))

(register-handler
 :update-score
 (fn [db [_ position]]
   (let [score (- 10 position)]
     (dispatch [:check-game-over score])
     (if (> score (-> db :game-state :score))
       (assoc-in db [:game-state :score] score)
       db))))
