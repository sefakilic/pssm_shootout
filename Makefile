data/NC000913.fna:
	curl ftp://ftp.ncbi.nih.gov/genomes/Bacteria/Escherichia_coli_K_12_substr__MG1655_uid57779/NC_000913.fna -o data/NC000913.fna

#Python
results/python_results.txt:langs/python_shootout/shootout.py
	langs/python_shootout/shootout.py data/NC000913.fna data/binding_sites.txt results/python_results.txt

#Clojure

