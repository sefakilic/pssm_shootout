"""
Python code for the shootout
"""
import sys
from math import log

def log2(x):
    return log(x,2)

def parse_genome(genome_file):
    """
    Return the genome as string
    """
    with open(genome_file) as f:
        genome = "".join([line.strip() for line in f.readlines()[1:]])
        # contains other iupac symbols
        return "".join(g for g in genome if g in "ATGC")

def parse_binding_sites(binding_site_file):
    """Return the binding sites as a list"""
    with open(binding_site_file) as f:
        binding_sites = [line.strip() for line in f.readlines()]
    return binding_sites

def make_pssm(binding_sites):
    """
    Return the PSSM as a list of dictionaries of the form:
    [{A:a_val,...,T:t_val}]
    """
    def transpose(xs):
        return zip(*xs)
    cols = transpose(binding_sites)
    n = float(len(binding_sites))
    return [{b:log2(((col.count(b)+1)/(n+4))/0.25)
             for b in "ACGT"}
            for col in cols]

def score(pssm,site):
    return sum(pssm[i][s] for i,s in enumerate(site))

def sliding_score(pssm,genome):
    n = len(pssm)
    scores = [score(pssm,genome[i:i+n]) for i in xrange(len(genome)-n+1)]
    return scores

if __name__ == "__main__":
    genome_file = sys.argv[1]
    binding_site_file = sys.argv[2]
    results_file = sys.argv[3]
    genome = parse_genome(genome_file)
    binding_sites = parse_binding_sites(binding_site_file)
    pssm = make_pssm(binding_sites)
    with open("shootout_python_results.txt",'w') as f:
        f.write("\n".join(map(str,sliding_score(pssm,genome))))


