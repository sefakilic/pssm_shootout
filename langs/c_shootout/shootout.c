#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

typedef struct {double a, c, g, t;} column;

char* parse_genome(const char* genome_file) {
  FILE* fp;
  char* genome = NULL;
  int i = 0;
  char c;
  int genome_max_sz = 100;

  fp = fopen(genome_file, "r");
  if (fp == NULL) {
    fprintf(stderr, "Couldn't open file %s\n", genome_file);
    exit(1);
  }

  /* skip first line */
  while((c = getc(fp)) != '\n');
  
  /* read the rest of the file */
  genome = (char*) malloc(genome_max_sz);
  
  while((c = getc(fp)) != EOF) {
    if (c == '\n')
      continue;
    /* if necessary, increase genome buffer size */
    if (i == genome_max_sz) {
      genome_max_sz *= 2;
      genome = (char*) realloc(genome, genome_max_sz * sizeof(char));
    }
    /* get char */
    genome[i++] = c;
  }
  return genome;
}

char** parse_binding_sites(const char* binding_site_file, int* num_binding_sites) {
  /* return the binding sites and the number of binding sites */
  FILE* fp;
  int max_binding_site_len = 100;
  char* line = malloc((max_binding_site_len+1) * sizeof(char));
  char** binding_sites = NULL;

  *num_binding_sites = 0;
  fp = fopen(binding_site_file, "r");
  if (fp == NULL) {
    fprintf(stderr, "Couldn't open file %s\n", binding_site_file);
    exit(1);
  }

  while(fgets(line, max_binding_site_len, fp) != NULL) {
    /* remove newline at the end */
    line[strlen(line)-1] = '\0';
    binding_sites = (char**) realloc(binding_sites, (*num_binding_sites+1)*sizeof(char*));
    binding_sites[*num_binding_sites] = (char*) malloc(max_binding_site_len);
    strcpy(binding_sites[*num_binding_sites], line);
    *num_binding_sites += 1;
  }
  return binding_sites;
}

double log2(double x) {
  return log(x) / log(2);
}


column* make_pssm(char** binding_sites, int num_sites, int* pssm_length) {
  /* Given list of sites and background probabilities, return the PSSM as a list of
  columns, each containing score for a,c,t,g.
  */
  int i, j;
  int site_length = strlen(binding_sites[0]);
  column *pssm = (column *) malloc(site_length * sizeof(column));
  column base_counts; /* base counts for a column */
  double total;
  
  /* count bases on each column */
  for (i = 0; i < site_length; i++) {
    base_counts.a = 0;
    base_counts.c = 0;
    base_counts.g = 0;
    base_counts.t = 0;
    
    for (j = 0; j < num_sites; ++j) {
      switch (binding_sites[j][i]) {
      case 'A':
      case 'a': base_counts.a += 1; break;
      case 'C':
      case 'c': base_counts.c += 1; break;
      case 'G':
      case 'g': base_counts.g += 1; break;
      case 'T':
      case 't': base_counts.t += 1; break;
      }
    }

    total = (base_counts.a + base_counts.c + base_counts.g + base_counts.t + 4);
    /* fill PSSM column */
    pssm[i].a = log2((base_counts.a+1) / total / 0.25);
    pssm[i].c = log2((base_counts.c+1) / total / 0.25);
    pssm[i].g = log2((base_counts.g+1) / total / 0.25);
    pssm[i].t = log2((base_counts.t+1) / total / 0.25);
    printf("%lf %lf %lf %lf\n", pssm[i].a, pssm[i].c, pssm[i].g, pssm[i].t);
  }
  *pssm_length = site_length; /* set the length of the pssm */
  return pssm;
}

double score(const column *pssm, int pssm_length, const char *seq) {
  /* Given PSSM and a sequence (of same length with PSSM, return score */
  int i;
  double score = 0.0;
  for (i = 0; i < pssm_length; ++i) {
    switch (seq[i]) {
    case 'A':
    case 'a': score += pssm[i].a; break;
    case 'C':
    case 'c': score += pssm[i].c; break;
    case 'G':
    case 'g': score += pssm[i].g; break;
    case 'T':
    case 't': score += pssm[i].t; break;
    }
  }
  return score;
}

double* sliding_score(const column* pssm, int pssm_length, const char* genome, int* len_scores) {
  /* Return pssm scores over the genome */

  /* don't compute the same thing in for loop! */
  int num_scores = strlen(genome) - pssm_length + 1;
  double* scores = (double*) malloc(num_scores * sizeof(double));
  char* seq = (char*) malloc((pssm_length+1) * sizeof(char));
  int i;
  for (i=0; i<num_scores; i++) {
    strncpy(seq, genome+i, pssm_length);
    seq[pssm_length] = 0;
    scores[i] = score(pssm, pssm_length, seq);
  }
  *len_scores = num_scores;
  return scores;
}

int main(int argc, char* argv[]) {
  const char* genome_file = argv[1];
  const char* binding_site_file = argv[2];
  const char* results_file = argv[3];
  char* genome;
  char** binding_sites;
  int num_binding_sites;
  column* pssm;
  int pssm_length;
  double* scores;
  int len_scores;
  FILE* fp;
  int i;

  genome = parse_genome(genome_file);
  binding_sites = parse_binding_sites(binding_site_file, &num_binding_sites);
  pssm = make_pssm(binding_sites, num_binding_sites, &pssm_length);
  scores = sliding_score(pssm, pssm_length, genome, &len_scores);
  fp = fopen(results_file, "w");
  for (i=0; i<len_scores; i++) {
    fprintf(fp, "%lf\n", scores[i]);
  }
  fclose(fp);
  
  return 0;
}
