(ns clojure-shootout.core
  (:gen-class))
(use '[clojure.string :only (split)])

(defn parse-genome
  "Return genome as string"
  []; no arguments
  (let [genome-file "../../data/NC_000913.fna"]
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

(defn make-pssm
  "make PSSM from binding sites"
  [binding-sites]
  (let [cols (transpose binding-sites)
        n (count binding-sites)]
    (for [col cols]
      (hash-for [b "ACGT"]
                [b (/ (count (filter (fn [x] (= x b)) col))
                      n)]))))

(defn sum [xs]
  (apply + xs))

(defn score
  "Score site with pssm"
  [pssm, site]
  (sum (for [[col-hash c] (zip pssm site)] (col-hash c))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
