(ns clojure_shootout.core
  (:gen-class))
(use '[clojure.string :only (split)])
(load "utils")

(defn parse-genome
  "Return genome as string"
  [genome-file]                         
  (->> genome-file
       slurp
       (drop-while (fn [c] (not (= c \newline))))
       (filter (fn [c] (some #{c} "ACGT")))
       (apply str)))

(defn parse-binding-sites
  "return binding sites as list of strings"
  [binding-site-file]                 
  (-> binding-site-file
      slurp
      (split #"\n")))

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
  [pssm site]
  (sum (for [[col-hash c] (zip pssm site)] (col-hash c))))

(defn slide-score
  "Score genome by pssm via sliding-window"
  [pssm genome]
  (let [width (count pssm)]
    (for [i (range (+ (count genome) (- width) 1))]
      (score pssm (subs genome i (+ i width))))))

(defn -main
  "Parse the genome file and binding sites, create the PSSM, score the
  genome and save the results"
  [& args]
  (let [genome-file (first args)
        binding-site-file (second args)
        results-file (third args)
        genome (parse-genome genome-file)
        binding-sites (parse-binding-sites binding-site-file)
        pssm (make-pssm binding-sites)
        scores (slide-score pssm genome)]
    (spit results-file
          (clojure.string/join "\n" scores))))
