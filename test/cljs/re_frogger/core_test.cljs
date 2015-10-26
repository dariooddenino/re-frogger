(ns re-frogger.core-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [re-frogger.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
