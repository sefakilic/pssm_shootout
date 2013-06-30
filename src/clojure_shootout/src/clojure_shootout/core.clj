(ns clojure-shootout.core
  (:gen-class))
(use '[clojure.string :only (split)])

(defn parse-genome
  "Return genome as string"
  []; no arguments
  (let [genome-file "../../data/NC000913.fna"]
    (->> genome-file
         slurp
         (drop-while (fn [c] (not (= c \newline))))
         (filter (fn [c] (some #{c} "ACGT")))
         (apply str))));hackish!

(defn parse-binding-sites
  "return binding sites as list of strings"
  []; no args
  (let [binding-site-file "../../data/binding_sites.txt"]
    (-> binding-site-file
        slurp
        (split #"\n"))))

(defn transpose
  [xxs]
  (apply mapv vector xxs))

(defn zip [xs ys]
  (transpose [xs ys]))

(defmacro hash-for-ref
  "Dictionary comprehension"
  [seq-expr body-expr]
  `(let [list-comp# (for ~seq-expr ~body-expr)
         ]
     (apply hash-map (apply concat list-comp#))))

(defmacro hash-for
  "Dictionary comprehension"
  [seq-expr body-expr]
  `(let [list-comp# (for ~seq-expr ~body-expr)
         ]
     (apply zipmap (transpose list-comp#))))

(def ln2 (Math/log 2))

(defn log2 [x]
  (/ (Math/log x) ln2))

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

(defn sum [xs]
  (apply + xs))


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
  "I don't do a whole lot ... yet."
  [& args]
  (let [genome (parse-genome)
        binding-sites (parse-binding-sites)
        pssm (make-pssm binding-sites)
        scores (slide-score pssm genome)]
    (spit "shootout_clojure_results.txt"
          (clojure.string/join "\n" scores))))
