(ns sss4clj.core
  (:require [incanter.stats :refer :all]
            [clojure.set :as s]
            [clojure.core.matrix :refer :all]
            [clojure.data.priority-map :refer :all]))


;Extend clojure.set namespace to includ a complement function
(intern 'clojure.set 'complement (fn [Omega A] (s/difference Omega A)))

(defn drop-1
  "Returns all sets formed by removal of 1 element from A."
  [A]
  (for [x A] (s/difference A #{x})))

(defn add-1
  "Returns all sets formed from the union of A with one
   element of Aᶜ."
  [Omega A]
  (let [Ac (s/complement Omega A)]
    (for [x Ac] (s/union #{x} A))))

(defn swap-1
  "Returns all sets formed from exchanging one element
   of A with one element of Aᶜ"
  [Omega A]
  (let [Ac (s/complement Omega A)
        A-drop-1 (drop-1 A)]
    (for [x Ac
          y A-drop-1]
      (s/union #{x} y))))

(defn neighbours
  "Returns all such sets formed from drop-1 add-1
   and swap-1 operations."
  [Omega A]
  [(drop-1 A) (add-1 Omega A) (swap-1 Omega A)])

(defn normalize
  "Returns normalized collection"
  [unnormalized]
  (let [Z (reduce + unnormalized)]
    (if (zero? Z)
      unnormalized
      (map  #(/ % Z) unnormalized))))

(defn rand-multinomial
  "Returns a multinomial draw of size 1 from collegtion
   categories with associated probabilities probs"
  [probs categories]
  (first (if (== (count categories) 1)
           categories
           (sample-multinomial 1 :probs (normalize probs) :categories (into (vector) categories)))))

(defn next-model
  "Performs one iteration of the sss algorithm"
  [score Omega A]
  (let [candidates (map #(rand-multinomial (pmap score %) %) (filter (complement empty?) (neighbours Omega A)))]
    (rand-multinomial (map score candidates) candidates)))

(defn add-priority-map
  "Returns a closure, namely, a function identical to
  objective that references a priority map to record
  the top max-count number of states. Reference to
  priority map added to meta-data of outputted function"
  [objective max-count]
  (let [scoreboard (atom (priority-map))]
    (with-meta (fn [x]
                 (let [fx (objective x)]
                   (do (swap! scoreboard assoc x fx)
                       (if (> (count @scoreboard) max-count) (swap! scoreboard pop))
                       fx))) {:scoreboard  scoreboard})))

(defn run-sss
  "A single function to perform sss. Outputs the final
  scoreboard of visited states as a priority-map"
  [objective initial-model Omega niter max-count]
  (let [score (add-priority-map objective max-count)
        last-iterate (last (take niter (iterate (partial next-model score Omega) initial-model)))
        final-scoreboard (:scoreboard (meta score))]
    @final-scoreboard))




