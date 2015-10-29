(ns re-frogger.db
  (:require [cljs.reader]
            [schema.core :as s :include-macros true]))

;; -- Default app-db Value

(def default-db
  {:name "re-frogger"
   :game-state {
                :running false
                :frame 0
                :score 0
                :game-over false
                :cars [{:x 0 :y 9 :s 1 :d true}
                       {:x 5 :y 8 :s 2 :d true}
                       {:x 6 :y 7 :s 3 :d true}
                       {:x 2 :y 6 :s 1 :d true}
                       {:x 14 :y 4 :s 2}
                       {:x 12 :y 3 :s 1}
                       {:x 10 :y 2 :s 3}
                       {:x 15 :y 1 :s 1}]
                :next-position {:x 7 :y 10}
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
