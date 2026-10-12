import java.util.List;

/** Immutable result of a local alignment. Positions are 1-based and inclusive (0 if no alignment). */
public final class AlignmentResult {
    public final String seqA, seqB;
    public final ScoringConfig cfg;
    public final int[][] matrix;          // full scoring matrix H, size (m+1) x (n+1)
    public final int score;
    public final int startA, endA, startB, endB;
    public final String alignedA, alignedB, midline;
    public final List<int[]> path;        // traceback cells (i, j) in forward order
    public final int matches, mismatches, gaps;

    public AlignmentResult(String seqA, String seqB, ScoringConfig cfg, int[][] matrix, int score,
                           int startA, int endA, int startB, int endB,
                           String alignedA, String alignedB, String midline, List<int[]> path,
                           int matches, int mismatches, int gaps) {
        this.seqA = seqA; this.seqB = seqB; this.cfg = cfg; this.matrix = matrix; this.score = score;
        this.startA = startA; this.endA = endA; this.startB = startB; this.endB = endB;
        this.alignedA = alignedA; this.alignedB = alignedB; this.midline = midline; this.path = path;
        this.matches = matches; this.mismatches = mismatches; this.gaps = gaps;
    }

    public boolean isEmpty() { return score == 0; }
    public int length() { return alignedA.length(); }
    public double identity() { return length() == 0 ? 0 : 100.0 * matches / length(); }

    /** Plain-text rendering (used by the CLI and the Save button). */
    public String toText(int width) {
        StringBuilder sb = new StringBuilder();
        sb.append("Scoring: ").append(cfg).append('\n');
        sb.append("Optimal local alignment score: ").append(score).append('\n');
        if (isEmpty()) {
            sb.append("No similar region found (best local score is 0).\n");
            return sb.toString();
        }
        sb.append(String.format("Region in Seq A: %d-%d | Region in Seq B: %d-%d%n", startA, endA, startB, endB));
        sb.append(String.format("Alignment length: %d | Matches: %d | Mismatches: %d | Gaps: %d | Identity: %.1f%%%n%n",
                length(), matches, mismatches, gaps, identity()));
        int pa = startA, pb = startB;
        for (int k = 0; k < length(); k += width) {
            int e = Math.min(length(), k + width);
            String sa = alignedA.substring(k, e), sm = midline.substring(k, e), sbb = alignedB.substring(k, e);
            int na = sa.replace("-", "").length(), nb = sbb.replace("-", "").length();
            sb.append(String.format("Seq A %5d  %s  %d%n", pa, sa, pa + na - 1));
            sb.append(String.format("%12s%s%n", "", sm));
            sb.append(String.format("Seq B %5d  %s  %d%n%n", pb, sbb, pb + nb - 1));
            pa += na; pb += nb;
        }
        sb.append("Legend: | match, : mismatch, - gap\n");
        return sb.toString();
    }
}
