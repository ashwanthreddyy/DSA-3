import java.util.List;

/** Builds the "Algorithm Comparison" text (problem-class analysis for CO1 / CO3). */
public final class AnalysisReport {
    private AnalysisReport() {}

    public static String build(String a, String b, ScoringConfig cfg, AlignmentResult sw) {
        int global = NeedlemanWunsch.score(a, b, cfg);
        String shorter = a.length() <= b.length() ? a : b;
        String longer = (shorter == a) ? b : a;
        List<Integer> hits = KMP.search(longer, shorter);

        StringBuilder s = new StringBuilder();
        s.append("ALGORITHM COMPARISON ON YOUR INPUT\n");
        s.append("==================================\n");
        s.append(String.format("Smith-Waterman (local)    : score %d -> best similar region (%d aligned columns)%n", sw.score, sw.length()));
        s.append(String.format("Needleman-Wunsch (global) : score %d -> forces both full sequences to align end to end%n", global));
        if (hits.isEmpty()) {
            s.append(String.format("KMP exact search          : the shorter sequence (length %d) does NOT occur as an exact substring of the longer one.%n", shorter.length()));
        } else {
            s.append(String.format("KMP exact search          : the shorter sequence occurs exactly at position %d of the longer one.%n", hits.get(0) + 1));
        }
        if (sw.score > global)
            s.append("\n-> The local score is higher than the global score: the sequences share a conserved region\n   but differ elsewhere, which is exactly the case local alignment is designed for.\n");

        s.append("\nWHY LOCAL DYNAMIC PROGRAMMING? (CO1 - problem-class signature)\n");
        s.append("--------------------------------------------------------------\n");
        s.append("* Exact matching (KMP, Z-function, Rabin-Karp) only finds identical substrings; real genes\n");
        s.append("  contain substitutions and insertions/deletions, so exact matching misses them.\n");
        s.append("* Global alignment (Needleman-Wunsch) penalises the unrelated flanking regions and can hide\n");
        s.append("  a short conserved region.\n");
        s.append("* Problem signature: 'best-scoring pair of substrings under edit-like scoring' has optimal\n");
        s.append("  substructure and overlapping subproblems -> dynamic programming with a reset-to-zero rule.\n");

        s.append("\nCOMPLEXITY (CO3 - advanced DP)\n");
        s.append("------------------------------\n");
        s.append("Smith-Waterman : Time O(m*n), Space O(m*n) (needed for the traceback)\n");
        s.append("Needleman-Wunsch: Time O(m*n), Space O(m*n)\n");
        s.append("KMP            : Time O(m+n), Space O(m) - but exact matches only\n");
        s.append("Space can be reduced to O(min(m,n)) with Hirschberg's divide-and-conquer technique.\n");
        s.append(String.format("%nThis run: m = %d, n = %d -> %,d matrix cells computed.%n", a.length(), b.length(), (long) a.length() * b.length()));
        return s.toString();
    }
}
