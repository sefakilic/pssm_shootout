function parse_genome(genome_file)
    # Parse genome file
    lines = open(readlines, genome_file)
    genome = mapreduce(rstrip, *, lines[2:])
    genome
end

function parse_binding_sites(binding_sites_file)
    # Parse binding site file
    lines = open(readlines, binding_sites_file)
    binding_sites = map(rstrip, lines)
    binding_sites
end

function chars(str)
    # convert string to an array of chars
    map(char, str.data)
end

function make_pssm(binding_sites)
    # Return the PSSM as list of dictionaries of the form
    # [{A: a_val, ..., T: t_val}]
    mat = mapreduce(site -> transpose(chars(site)), vcat, binding_sites)
    n = length(binding_sites)
    pssm = [[b => log2(((count(x -> x==b, mat[:,col])+1) / (n+4)) / 0.25) for b in "ACGT"]
            for col=1:size(mat,2)]
    pssm
end

function score(pssm, site)
    # Score a given site
    n = length(pssm)
    sum([pssm[i][site[i]] for i=1:n])
end

function sliding_score(pssm, genome)
    # Score entire genome
    n = length(pssm)
    scores = [score(pssm, genome[i:i+n-1]) for i=1:length(genome)-n+1]
    scores
end

if !isinteractive()
    genome_file = ARGS[1]
    binding_site_file = ARGS[2]
    results_file = ARGS[3]
    genome = parse_genome(genome_file)
    binding_sites = parse_binding_sites(binding_site_file)
    pssm = make_pssm(binding_sites)
    f = open(results_file, "w")
    write(f, join(map(string, sliding_score(pssm, genome)), '\n'))
    close(f)
end
