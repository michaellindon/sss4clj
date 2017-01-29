(ns sss4clj.core
  (:require [incanter.stats :refer :all]
            [clojure.set :as s]
            [clojure.core.matrix :refer :all]
            [clojure.data.priority-map :refer :all]))


;Extend clojure.set namespace to includ a complement function
(intern 'clojure.set 'complement (fn [Omega A] (s/difference Omega A)))

(defn drop-1 [A] (for [x A] (s/difference A #{x})))

(defn add-1 [Omega A] (let [Ac (s/complement Omega A)]
                        (for [x Ac] (s/union #{x} A))))

(defn swap-1 [Omega A] (let [Ac (s/complement Omega A)
                             A-drop-1 (drop-1 A)]
                         (for [x Ac
                               y A-drop-1]
                           (s/union #{x} y))))

(defn neighbours [Omega A] [(drop-1 A) (add-1 Omega A) (swap-1 Omega A)])

(defn normalize [unnormalized]
  (let [Z (reduce + unnormalized)]
    (if (zero? Z)
      unnormalized
      (map  #(/ % Z) unnormalized))))

(defn rand-multinomial [probs categories]
  (first (if (== (count categories) 1)
           categories
           (sample-multinomial 1 :probs (normalize probs) :categories (into (vector) categories)))))

(defn next-model [score Omega A]
  (let [candidates (map #(rand-multinomial (pmap score %) %) (filter (complement empty?) (neighbours Omega A)))]
    (rand-multinomial (map score candidates) candidates)))

(defn add-priority-map [objective max-count]
  (let [scoreboard (atom (priority-map))]
    (with-meta (fn [x]
                 (let [fx (objective x)]
                   (do (swap! scoreboard assoc x fx)
                       (if (> (count @scoreboard) max-count) (swap! scoreboard pop))
                       fx))) {:scoreboard  scoreboard})))

(defn run-sss [objective Omega niter max-count]
  (let [score (add-priority-map objective max-count)
        last-iterate (last (take niter (iterate (partial next-model score Omega) #{})))
        final-scoreboard (:scoreboard (meta score))]
    @final-scoreboard))




