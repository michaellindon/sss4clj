(ns sss4clj.kl
  (:require [sss4clj.core :refer :all]
            [clojure.core.matrix :refer :all]
            [incanter.stats :refer :all]
            [clojure.core.matrix.linear :as la]))

;; We need to be able to sample and evaluate a non standard t distribution
;; For this lets define some helper functions
(defn logpdf-nst
  "Unnormalized logpdf of non standard t"
  [x mu sigma df]
  (let [z (/ (- x mu) sigma)]
    (negate (+ (log sigma) (* (/ (inc df) 2) (log (inc (/ (square z) df))))))))

(defn sample-nst
  "Generate non standard t random variate"
  [n mu sigma df]
  (add mu (mul sigma (sample-t n :df df))))

(defn KL
  "Returns an objective function defined by prior parameters"
  [p-set X prior-mu prior-sigma scaler df]
  (let [p (into [] p-set)
        Xp (select X p)
        p-mean (dot Xp (select prior-mu p))
        p-sigma (if (empty? p)
                  (square scaler)
                  (* (square scaler) (inc (dot Xp (mmul (inverse (select prior-sigma p p)) Xp)))))
        draws (sample-nst 10000 p-mean (sqrt p-sigma) df)
        p-logpdf (fn [x] (logpdf-nst x p-mean (sqrt p-sigma) df))]
    (fn [n-set]
      (let [n (into [] n-set)
            Xn (select X n)
            n-mean (dot Xn (select prior-mu n))
            n-sigma (if (empty? n)
                      (square scaler)
                      (* (square scaler) (inc (dot Xn (mmul (inverse (select prior-sigma n n)) Xn)))))
            n-logpdf (fn [x] (logpdf-nst x n-mean (sqrt n-sigma) df))
            difference (fn [x] (- (p-logpdf x) (n-logpdf x)))]
        (mean (map difference draws))))))



;; Lets test the algorithm for random prior parameters and previous model
;(def dim 10)
;(def L (reshape (matrix (sample-normal (* dim dim))) [dim dim]))
;(def prior-sigma (mmul (transpose L) L))
;(def prior-mu (sample-normal dim))
;(def previous-model #{0 1 4})
;(def X (sample-normal dim))

;(def divergence (KL previous-model X prior-mu prior-sigma 1 1))
;(def obj-fn (comp exp negate (partial * 10) divergence))
;(def Omega (range 0 dim))
;(obj-fn Omega)
;(obj-fn previous-model)
;(obj-fn #{4 1})
;(def Omega (set (range 0 dim)))

;(run-sss obj-fn Omega 100 10)
