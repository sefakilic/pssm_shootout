(ns clojure_shootout.utils)

(defn transpose
  [xxs]
  (apply mapv vector xxs))

(defn zip [xs ys]
  (transpose [xs ys]))

(defmacro hash-for
  "Dictionary comprehension.  Usage:(hash-for [x xs] [key-expr val-expr])"
  [seq-expr body-expr]
  `(let [list-comp# (for ~seq-expr ~body-expr)]
     (apply zipmap (transpose list-comp#))))

(def ln2 (Math/log 2))

(defn log2 [x]
  (/ (Math/log x) ln2))

(defn sum [xs]
  (reduce + xs)) ;; better than (apply + xs)

(defn third [xs]
  (nth xs 2))

(def ^:dynamic *times* nil)

(defmacro timek
  "Evaluates expr and prints the time it took.  Returns the value of
 expr."
  {:added "1.0"}
  [k expr]
  (let [plus (fn [val val2] (if val (+ val val2) val2))]
    `(let [start# (. System (nanoTime))
           ret# ~expr
           t# (/ (double (- (. System (nanoTime)) start#)) 1000000.0)]
       (swap! *times* update-in [~k] ~plus t#)
       ret#)))

(defmacro with-timek
  [& body]
  `(binding [*times* (atom {})]
     ~@body
     (clojure.pprint/pprint @*times*)))
