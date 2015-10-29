(ns re-frogger.db
  (:require [cljs.reader]
            [schema.core :as s :include-macros true]))

;; -- Default app-db Value

(def default-db
  {:name "re-frogger"
   :game-state {
                :running false
                :score 0
                :game-over false
                :lane-types [1 1 1 1 1 1 1 1 1 1 0]
                :position {:x 7 :y 10}}})

;; -- Db schema TODO: add validations

(def schema {;; App name
             :name s/Str
             ;; Game state
             :game-state {;; Is the game running?
                          :running s/Bool
                          ;; The current score
                          :score s/Int}})

;; -- Local Storage

(def lsk "re-frogger")
