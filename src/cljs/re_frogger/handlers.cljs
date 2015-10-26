(ns re-frogger.handlers
    (:require [re-frame.core :as re-frame]
              [re-frogger.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
