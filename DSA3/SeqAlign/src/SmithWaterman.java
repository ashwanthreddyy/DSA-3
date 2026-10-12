import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Smith-Waterman local sequence alignment (dynamic programming).
 *
 *   H[i][j] = max( 0,
 *                  H[i-1][j-1] + s(a[i], b[j]),   // match / mismatch
 *                  H[i-1][j]   + gap,             // gap in B
 *                  H[i][j-1]   + gap )            // gap in A
 *
 * The best local alignment ends at the cell with the maximum score and is recovered by a
 * traceback that stops as soon as a cell with score 0 is reached.
 * Time: O(m*n)   Space: O(m*n)
 */
public final class SmithWaterman {
    private SmithWaterman() {}

    public static AlignmentResult align(String a, String b, ScoringConfig cfg) {
        int m = a.length(), n = b.length();
        int[][] H = new int[m + 1][n + 1];          // row 0 and column 0 stay 0
        int best = 0, bi = 0, bj = 0;

        // 1) Fill the scoring matrix and remember the maximum cell
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int diag = H[i - 1][j - 1] + cfg.score(a.charAt(i - 1), b.charAt(j - 1));
                int up   = H[i - 1][j] + cfg.gap;
                int left = H[i][j - 1] + cfg.gap;
                int v = Math.max(0, Math.max(diag, Math.max(up, left)));
                H[i][j] = v;
                if (v > best) { best = v; bi = i; bj = j; }
            }
        }

        if (best == 0) {   // no similarity at all
            return new AlignmentResult(a, b, cfg, H, 0, 0, 0, 0, 0, "", "", "",
                    new ArrayList<>(), 0, 0, 0);
        }

        // 2) Traceback from the max cell until a 0 is reached
        StringBuilder sa = new StringBuilder(), sb = new StringBuilder(), mid = new StringBuilder();
        List<int[]> path = new ArrayList<>();
        int i = bi, j = bj, matches = 0, mismatches = 0, gaps = 0;
        while (i > 0 && j > 0 && H[i][j] > 0) {
            path.add(new int[]{i, j});
            int cur = H[i][j];
            char x = a.charAt(i - 1), y = b.charAt(j - 1);
            if (cur == H[i - 1][j - 1] + cfg.score(x, y)) {            // diagonal
                sa.append(x); sb.append(y);
                if (x == y) { mid.append('|'); matches++; } else { mid.append(':'); mismatches++; }
                i--; j--;
            } else if (cur == H[i - 1][j] + cfg.gap) {                  // up: gap in B
                sa.append(x); sb.append('-'); mid.append(' '); gaps++;
                i--;
            } else {                                                    // left: gap in A
                sa.append('-'); sb.append(y); mid.append(' '); gaps++;
                j--;
            }
        }
        Collections.reverse(path);
        return new AlignmentResult(a, b, cfg, H, best, i + 1, bi, j + 1, bj,
                sa.reverse().toString(), sb.reverse().toString(), mid.reverse().toString(),
                path, matches, mismatches, gaps);
    }
}
