(defn transpose
  [xxs]
  (apply mapv vector xxs))

(defn zip [xs ys]
  (transpose [xs ys]))

(defmacro hash-for
  "Dictionary comprehension.  Usage:(hash-for [x xs] [key-expr val-expr])"
  [seq-expr body-expr]
  `(let [list-comp# (for ~seq-expr ~body-expr)
         ]
     (apply zipmap (transpose list-comp#))))

(def ln2 (Math/log 2))

(defn log2 [x]
  (/ (Math/log x) ln2))

(defn sum [xs]
  (apply + xs))

(defn third [xs]
  (nth xs 2))
