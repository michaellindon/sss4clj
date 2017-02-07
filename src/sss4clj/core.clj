(ns sss4clj.core
  (:require [incanter.stats :refer :all]
            [clojure.set :as s]
            [clojure.core.matrix :refer :all]
            [clojure.data.priority-map :refer :all]))


;Extend clojure.set namespace to includ a complement function
(intern 'clojure.set 'complement (fn [Omega A] (s/difference Omega A)))

(defn drop-1 [A]
  "Returns all sets formed by removal of 1 element from A."
  (for [x A] (s/difference A #{x})))

(defn add-1 [Omega A]
  "Returns all sets formed from the union of A with one
   element of Aᶜ."
  (let [Ac (s/complement Omega A)]
    (for [x Ac] (s/union #{x} A))))

(defn swap-1 [Omega A]
  "Returns all sets formed from exchanging one element
   of A with one element of Aᶜ"
  (let [Ac (s/complement Omega A)
        A-drop-1 (drop-1 A)]
    (for [x Ac
          y A-drop-1]
      (s/union #{x} y))))

(defn neighbours [Omega A]
  "Returns all such sets formed from drop-1 add-1
   and swap-1 operations."
  [(drop-1 A) (add-1 Omega A) (swap-1 Omega A)])

(defn normalize [unnormalized]
  "Returns normalized collection"
  (let [Z (reduce + unnormalized)]
    (if (zero? Z)
      unnormalized
      (map  #(/ % Z) unnormalized))))

(defn rand-multinomial [probs categories]
  "Returns a multinomial draw of size 1 from collegtion
   categories with associated probabilities probs"
  (first (if (== (count categories) 1)
           categories
           (sample-multinomial 1 :probs (normalize probs) :categories (into (vector) categories)))))

(defn next-model [score Omega A]
  "Performs one iteration of the sss algorithm"
  (let [candidates (map #(rand-multinomial (pmap score %) %) (filter (complement empty?) (neighbours Omega A)))]
    (rand-multinomial (map score candidates) candidates)))

(defn add-priority-map [objective max-count]
  "Returns a closure, namely, a function identical to
  objective that references a priority map to record
  the top max-count number of states. Reference to
  priority map added to meta-data of outputted function"
  (let [scoreboard (atom (priority-map))]
    (with-meta (fn [x]
                 (let [fx (objective x)]
                   (do (swap! scoreboard assoc x fx)
                       (if (> (count @scoreboard) max-count) (swap! scoreboard pop))
                       fx))) {:scoreboard  scoreboard})))

(defn run-sss [objective Omega niter max-count]
  "A single function to perform sss. Outputs the final
  scoreboard of visited states as a priority-map"
  (let [score (add-priority-map objective max-count)
        last-iterate (last (take niter (iterate (partial next-model score Omega) #{})))
        final-scoreboard (:scoreboard (meta score))]
    @final-scoreboard))




