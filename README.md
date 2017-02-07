	               ___      _ _ 
	              /   |    | (_)
	 ___ ___ ___ / /| | ___| |_ 
	/ __/ __/ __/ /_| |/ __| | |
	\__ \__ \__ \___  | (__| | |
	|___/___/___/   |_/\___|_| |
	                        _/ |
	                       |__/ 
[![Clojars Project](https://img.shields.io/clojars/v/sss4clj.svg)](https://clojars.org/sss4clj)

[![Build Status](https://travis-ci.org/michaellindon/sss4clj.svg?branch=master)](https://travis-ci.org/michaellindon/sss4clj)
[![Dependency Status](https://www.versioneye.com/user/projects/589901721e07ae0048c8e48e/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/589901721e07ae0048c8e48e)

##About
Shotgun Stochastic Search is an algorithm for maximizing functions {0,1}ᴾ → ℝ described in
[Shotgun Stochastic Search for “Large p” Regression](http://www.tandfonline.com/doi/abs/10.1198/016214507000000121).
Each element a ∈ {0,1}ᴾ can be uniquely identified by an "active set" i.e. {i ∈ ℕ| aᵢ = 1}. This project makes
the design choice to work with the latter.

##Installation
Leiningen:

Add the following dependency in your project.clj file

[![Clojars Project](https://clojars.org/sss4clj/latest-version.svg)](http://clojars.org/sss4clj)

##Usage

An example.clj file can be found in the source directory which one can experiment with in the repl.

```clojure

(ns sss4clj.example
  (:require [sss4clj.core :refer :all]
            [clojure.math.combinatorics :as combo]))

;; Let A ⊆ Ω ⊂ ℕ
(def A #{0 3 4})
(def Omega #{0 1 2 3 4})

;; use drop-1 to get all sets formed by removing 1 element
(drop-1 A)
=> (#{4 3} #{0 3} #{0 4})

;; drop-1 of empty set is considered empty
(drop-1 #{})
=> ()

;; use add-1 to get all sets formed from the union of A
;; with one element of Ω\A.
(add-1 Omega A)
=> (#{0 1 4 3} #{0 4 3 2})

;; add-1 of Omega is considered empty
(add-1 Omega Omega)
=> ()

;; use swap-1 to get all sets formed from exchanging one element
;; of A with one element of its Ω\A
(swap-1 Omega A)
=> (#{1 4 3} #{0 1 3} #{0 1 4} #{4 3 2} #{0 3 2} #{0 4 2})

;; use neighbours to obtain all such sets formed form drop-1
;; add-1 and swap-1 operations
(neighbours Omega A)
=> [(#{4 3} #{0 3} #{0 4}) (#{0 1 4 3} #{0 4 3 2}) (#{1 4 3} #{0 1 3} #{0 1 4} #{4 3 2} #{0 3 2} #{0 4 2})]

;;; To illustrate the next sss4clj functions we need a
;; mathematical objective function that maps sets A ⊆ Ω to R.

;; To do this lets just zip all sets in the power set of Ω
;; with a random uniform and convert to a function
(def subvalues (into (hash-map) (map vector (map #(set %) (combo/subsets (into (vector) Omega))) (repeatedly #(rand)))))
(defn objective [x] (subvalues x))

;; objective is now the function which we will try to maximize
(objective #{})
=> 0.5809422699383645
(objective Omega)
=> 0.37949346847568177
(objective A)
=> 0.1971199058962515

;; add-priority-map returns a new function that on the surface
;; is identical to objective
(def max-count 2)
(def score (add-priority-map objective max-count))
(score #{})
=> 0.5809422699383645

;; but score has a priority-map appended to its meta-data for
;; the purposes of recording and tracking the best values so far
(deref (:scoreboard (meta score)))
=> {#{} 0.5809422699383645}

;; sss may run for a long time and so optionally the user may
;; only want to keep track of the top `max-count` vals in memory.
;; This is the purpose of the priority map.
(objective A)
=> 0.1971199058962515
(deref (:scoreboard (meta score)))
=> {#{0 4 3} 0.1971199058962515, #{} 0.5809422699383645}
(score Omega)
=> 0.37949346847568177
(deref (:scoreboard (meta score)))
=> {#{0 1 4 3 2} 0.37949346847568177, #{} 0.5809422699383645}

;; Once the size of the priority-map becomes large than max-count
;; it drops the key-value pair with the smallest value. In the
;; previous example the key-value pair with key A was dropped.

;; Given a score function, an initial set A, and Ω, the user
;; can perform one iteration of sss
(next-model score Omega A)
=> #{0 4 2}

;; The output is a new model, but one can check the sets visited
;; within the iteration by dereferencing the meta-data of score
(deref (:scoreboard (meta score)))
=> {#{0 3 2} 0.8143170162925805, #{0 4} 0.8948151339032558}

;; Alternatively, the user can abstract the fine-grained details
;; of the implementation and call a one function do-all
;; This returns the final scoreboard of sets visited after
;; 100 iterations of sss with a max-count of 5
(run-sss objective Omega 100 5)
=> {#{0 1 4 3} 0.8108222058800102, #{0 3 2} 0.8143170162925805, #{0 4} 0.8948151339032558, #{0} 0.951193987214408, #{1} 0.9658085016841405}

```

## License

Copyright © 2017 Michael Lindon

Distributed under the Eclipse Public License either version 1.0
