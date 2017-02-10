# Example: Kullback Leibler Divergence

In the context of Bayesian variable selection suppose we have a generative
model for our data which is fully parameterized by an active set A ∈ 2ᴺ.
In some cases, given an active set A, we would like to find alternative
models, parameterized by an differenct active set B, which have similar
predictive properties. To establish an optimization problem, lets try
to find models which minimize the Kullback-Leibler divergence from
the model under consideration A.

## Establishing some helper functions
Lets assume the generative model is a non standard t-distribution. To
work with this we need to write a few functions 

```clojure
(defn logpdf-nst
  "Unnormalized logpdf of non standard t"
  [x mu sigma df]
  (let [z (/ (- x mu) sigma)]
    (negate (+ (log sigma) (* (/ (inc df) 2) (log (inc (/ (square z) df))))))))

(defn sample-nst
  "Generate non standard t random variate"
  [n mu sigma df]
  (add mu (mul sigma (sample-t n :df df))))
```

Now lets write a function KL which maps a number of arguments which define
the generative model to a function 2ᴺ -> R.

```clojure
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

```


## Simulating model parameters
```clojure
(def dim 10)
(def L (reshape (matrix (sample-normal (* dim dim))) [dim dim]))
(def prior-sigma (mmul (transpose L) L))
(def prior-mu (sample-normal dim))
(def previous-model #{0 1 4})
(def X (sample-normal dim))
```

## Obtain objective function
Note the current formulation is a minimization problem, namely, trying to minimize the KL
divergence from the previous model, whilst sss4clj expects a maximization problem. Note
inserting a minus sign is not quite enough because the objective function values given
a set of candidate active sets must be amenable to renormalization such that one can
sample one of these candidates from a multinomial distribution. To this end one must
compose the divergence (with an optional scaling factor) with negation and exponentiation.
```clojure
(def divergence (KL previous-model X prior-mu prior-sigma 1 1))
(def obj-fn (comp exp negate (partial * 10) divergence))
```

## Run-sss
Run for 100 iterations keeping track of the 10 best candidates
```clojure
(def Omega (range 0 dim))
(run-sss obj-fn Omega 100 10)
```
## License

Copyright © 2017 Michael Lindon

Distributed under the Eclipse Public License either version 1.0
