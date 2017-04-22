(ns sss4clj.example
  (:require [sss4clj.core :refer :all]
            [clojure.math.combinatorics :as combo]
            [clojure.set :as set]))

(. (new org.apache.commons.math3.analysis.integration.RombergIntegrator) integrate 10000 (fn [x] x) 0 1)
(. (new org.apache.commons.math3.analysis.integration.RombergIntegrator) setup) 
(def foo (new org.apache.commons.math3.analysis.integration.SimpsonIntegrator))
(def myfun (fn [x] (* x x)))
(. foo integrate 10 myfun 0 1)
()
(type org.apache.commons.math3.analysis.FunctionUtils)
(use org.apache.common.math3)

(new org.apache.commons.math3.analysis.UnivariaterealFunction)
(def optimizer (new org.apache.commons.math3.optimization.linear.SimplexSolver 0.00001 0.000000000001))
(. optimizer setSimplex (new org.apache.commons.math3.optimization.direct.NelderMeadSimplex [0.2 0.2]) )

(def optim (new org.apache.commons.math3.optimization.direct.PowellOptimizer 0.00000001 0.00000000000001))
(new MaxEval 1000)
(. optim optimize (new MaxEval(1000)) (new ObjectiveFunction (fn [x y] (+ (* x x) (y y)))) GoalType.MINIMIZE (new InitialGuess([ 0 0 ])))
(.newInstance org.apache.commons.math3.analysis.UnivariateFunction)

(def Omega #{0 1 2 3 4 5 6 7 8 9})

(def pset (combo/subsets (into [] Omega)))
(def vin (into (hash-map) (map vector Omega (map rand Omega))))
(def vout (into (hash-map) (map vector Omega (map rand Omega))))
(defn fin [x] (vin x))
(defn fout [x] (vout x))
(defn objective [x] (+ (reduce + 0 (map fout (set/difference Omega x))) (reduce + 0 (map fin x))))
(defn mypred [x y] (> (+ (fin x) (fout y)) (+ (fin y) (fout x))))

(sort mypred (into [] Omega))
(sort (fn [x y] (> (second x) (second y))) (filter (fn [z] (= 3 (count (first z)))) (map vector pset (map objective pset))))

(import org.apache.commons.math3.analysis.integration.gauss)
(import org.apache.commons)
(defn testf []
  (reify
    org.apache.commons.math3.analysis.UnivariateFunction
    (value [x] (* 2 x))))

(def foo (new org.apache.commons.math3.distribution.NormalDistribution 10 0.01))
(.getSampler foo)


(.value (new org.apache.commons.math3.analysis.function.Abs) -3)
(def foo (reify org.apache.commons.math3.analysis.UnivariateFunction
  (value [this x]
    x)))
(instance? org.apache.commons.math3.analysis.UnivariateFunction foo)
(.value foo 2)
(def my-integrator (new org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator))
(def my-integrator org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator)
(.integrate my-integrator 100 foo Double/NEGATIVE_INFINITY Double/POSITIVE_INFINITY)

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


