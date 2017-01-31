(defproject sss4clj "0.1.0-SNAPSHOT"
  :description "Shotgun Stochastic Search Algorithm"
  :url "https://github.com/michaellindon/sss4clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter "1.9.1"]
                 [net.mikera/core.matrix "0.57.0" :exclusions [org.clojure/clojure]]
                 [net.mikera/vectorz-clj "0.45.0" :exclusions [org.clojure/clojure]]
                 [org.clojure/test.check "0.9.0" :exclusions [org.clojure/clojure]]
		 [org.clojure/math.combinatorics "0.1.3" :exclusions [org.clojure/clojure]]
                 [org.clojure/data.priority-map "0.0.7" :exclusions [org.clojure/clojure]]])
