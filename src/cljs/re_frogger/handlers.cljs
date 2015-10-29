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
(def RBUTTON 114)

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
 :handle-keyboard
 (fn [db [_ key-pressed]]
   (let [position (move-player (-> db :game-state :position) key-pressed)
         game-over (-> db :game-state :game-over)]
     (if game-over
       (if (= key-pressed RBUTTON)
         (assoc-in db/default-db [:game-state :running] true)
         db)
       (do (dispatch [:update-score (:y position)])
           (assoc-in db [:game-state :next-position] position))))))

(register-handler
 :update-position
 (fn [db [_ _]]
   (let [next-position (-> db :game-state :next-position)]
     (assoc-in db [:game-state :position] next-position))))

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

(register-handler
 :start-game
 (fn [db [_ _]]
   (assoc-in db [:game-state :running] true)))

(defn- update-car [car]
  (if (:d car)
    (if (= (:x car) 14)
      nil
      (assoc car :x (inc (:x car))))
    (if (= (:x car) 0)
      nil
      (assoc car :x (dec (:x car))))))


(defn- update-traffic [cars frame]
  (map (fn [car] 
         (if (= 0 (mod frame (:s car)))
           (update-car car)
           car))
       cars))

(def not-nil? (complement nil?))

(register-handler
 :update-traffic
 (fn [db [_ _]]
   (let [cars (-> db :game-state :cars)
         frame (-> db :game-state :frame)]
     (assoc-in db [:game-state :cars] (filter not-nil? (update-traffic cars frame))))))

(register-handler 
 :check-collisions
 (fn [db [_ _]]
   (let [cars (-> db :game-state :cars)
         pos (-> db :game-state :position)]
     (if (some #(and (= (:x %) (:x pos))
                     (= (:y %) (:y pos)))
               cars)
       (assoc-in db [:game-state :game-over] true)
       db))))

(defn rand-speed []
  (int (inc (rand 3))))

(defn- new-cars [cars]
  (conj cars
        {:x 0 :y (int (+ 6 (rand 4))) :s (rand-speed) :d true}
        {:x 14 :y (int (+ 1 (rand 4))) :s (rand-speed)})
)

(register-handler
 :generate-cars
 (fn [db [_ _]]
   ;; Creates a car per side
   (let [cars (-> db :game-state :cars)
         frame (-> db :game-state :frame)]
     (if (= 0 (mod frame 3))
       (assoc-in db [:game-state :cars] (new-cars cars))
       db))))
