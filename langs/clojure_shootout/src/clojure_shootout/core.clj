(ns clojure_shootout.core
  (:require [clojure.string :as s]
            [clojure_shootout.utils :refer :all]))

(defn parse-genome
  "Return genome as string"
  [genome-file]                         
  (->> genome-file
       slurp
       (drop-while (fn [c] (not (= c \newline))))
       (filter (set "ACGT"))
       (apply str)))

(defn parse-binding-sites
  "return binding sites as list of strings"
  [binding-site-file]                 
  (-> binding-site-file
      slurp
      (s/split #"\n")))

(defn make-pssm
  "make PSSM from binding sites"
  [binding-sites]
  (let [cols (transpose binding-sites)
        n (count binding-sites)]
    (for [col cols]
      (hash-for [b "ACGT"]
                [b (log2 (/ (float (/ (+ (count (filter (fn [x] (= x b)) col)) 1)
                                      (+ n 4)))
                            0.25))]))))

(defn score
  "Score site with pssm"
  ;; refactored by gtrak -- reduce beats sum?
  [pssm site]
  (reduce (fn [acc [col-hash c]]
            (+ acc (long (col-hash c))))
          0
          (zip pssm site)))

(defn slide-score
  "Score genome by pssm via sliding-window"
  [pssm genome]
  (let [width (count pssm)]
    ;gtrak showed me this nice little piece of syntax for the for
    ;macro: the let keyword.  Haskell has similar syntax for let in
    ;list comps; would be nice to see it in Python one day.
    (for [i (range (+ (count genome) (- width) 1))
          :let [site (subs genome i (+ i width))]] 
      (score pssm site))))

(defn -main
  "Parse the genome file and binding sites, create the PSSM, score the
  genome and save the results"
  [& args]
  (let [genome-file (first args)
        binding-site-file (second args)
        results-file (third args)
        ;; doall forces evaluation of lazy seq.  Thanks to gtrak for improvement.
        genome (doall (parse-genome genome-file))
        binding-sites (parse-binding-sites binding-site-file)
        pssm (doall (make-pssm binding-sites))
        _ (println "Slide-score") ;;nonce assignment for timing
        scores (doall (slide-score pssm genome))]
    (spit results-file
          (s/join "\n" scores))))

(comment
  (with-timek (timek :main (-main "../../data/NC000913.fna" "../../data/binding_sites.txt" "../../results/clojure_results.txt")))
 )
