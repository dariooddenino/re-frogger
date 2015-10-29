(ns re-frogger.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :refer [register-sub] :as re-frame]))

(register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

(register-sub
 :score
 (fn [db]
   (reaction (-> @db :game-state :score))))

(register-sub
 :game-over
 (fn [db]
   (reaction (-> @db :game-state :game-over))))

(register-sub
 :lane-types
 (fn [db]
   (reaction (-> @db :game-state :lane-types))))

(register-sub
 :position
 (fn [db]
   (reaction (-> @db :game-state :position))))

(register-sub
 :frame
 (fn [db]
   (reaction (-> @db :game-state :frame))))
