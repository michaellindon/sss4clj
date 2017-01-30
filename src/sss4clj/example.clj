(ns sss4clj.example
  (:require [sss4clj.core :refer :all]
            [clojure.math.combinatorics :as combo]))

;; Let A ⊆ Ω ⊂ ℕ
(def A #{0 3 4})
(def Omega #{0 1 2 3 4})

;; use drop-1 to get all sets formed by removing 1 element
(drop-1 A)

;; drop-1 of empty set is considered empty
(drop-1 #{})

;; use add-1 to get all sets formed from the union of A
;; with one element of Ω\A.
(add-1 Omega A)

;; add-1 of Omega is considered empty
(add-1 Omega Omega)

;; use swap-1 to get all sets formed from exchanging one element
;; of A with one element of its Ω\A
(swap-1 Omega A)

;; use neighbours to obtain all such sets formed form drop-1
;; add-1 and swap-1 operations
(neighbours Omega A)

;;; To illustrate the next sss4clj functions we need a
;; mathematical objective function that maps sets A ⊆ Ω to R.

;; To do this lets just zip all sets in the power set of Ω
;; with a random uniform and convert to a function
(def subvalues (into (hash-map) (map vector (map #(set %) (combo/subsets (into (vector) Omega))) (repeatedly #(rand)))))
(defn objective [x] (subvalues x))

;; objective is now the function which we will try to maximize

(objective #{})
(objective Omega)
(objective A)

;; add-priority-map returns a new function that on the surface
;; is identical to objective
(def max-count 2)
(def score (add-priority-map objective max-count))
(score #{})

;; but score has a priority-map appended to its meta-data for
;; the purposes of recording and tracking the best values so far
(deref (:scoreboard (meta score)))

;; sss may run for a long time and so optionally the user may
;; only want to keep track of the top `max-count` vals in memory.
;; This is the purpose of the priority map.
(objective A)
(deref (:scoreboard (meta score)))
(score Omega)
(deref (:scoreboard (meta score)))

;; Once the size of the priority-map becomes large than max-count
;; it drops the key-value pair with the smallest value

;; Given a score function, an initial set A, and Ω, the user
;; can perform one iteration of sss
(next-model score Omega A)

;; The output is a new model, but one can check the sets visited
;; within the iteration by dereferencing the meta-data of score
(deref (:scoreboard (meta score)))

;; Alternatively, the user can abstract the fine-grained details
;; of the implementation and call a one function do-all
;; This returns the final scoreboard of sets visited
(run-sss objective Omega 100 10)


