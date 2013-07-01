data/NC000913.fna:
	curl ftp://ftp.ncbi.nih.gov/genomes/Bacteria/Escherichia_coli_K_12_substr__MG1655_uid57779/NC_000913.fna -o data/NC000913.fna

#Python
results/python_results.txt:langs/python_shootout/
	time python langs/python_shootout/shootout.py data/NC000913.fna data/binding_sites.txt results/python_results.txt

#Clojure
langs/clojure_shootout/target/clojure_shootout-0.1.0-SNAPSHOT-standalone.jar:langs/clojure_shootout/src
	cd langs/clojure_shootout
	lein uberjar
	cd ../..

results/clojure_results.txt: langs/clojure_shootout/target/clojure_shootout-0.1.0-SNAPSHOT-standalone.jar
	time java -jar langs/clojure_shootout/target/clojure_shootout-0.1.0-SNAPSHOT-standalone.jar data/NC000913.fna data/binding_sites.txt results/clojure_results.txt

all:  results/clojure_results.txt
