/** Global alignment score (Needleman-Wunsch), used only for comparison with Smith-Waterman. */
public final class NeedlemanWunsch {
    private NeedlemanWunsch() {}

    public static int score(String a, String b, ScoringConfig cfg) {
        int m = a.length(), n = b.length();
        int[][] F = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) F[i][0] = i * cfg.gap;
        for (int j = 1; j <= n; j++) F[0][j] = j * cfg.gap;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int diag = F[i - 1][j - 1] + cfg.score(a.charAt(i - 1), b.charAt(j - 1));
                F[i][j] = Math.max(diag, Math.max(F[i - 1][j] + cfg.gap, F[i][j - 1] + cfg.gap));
            }
        }
        return F[m][n];
    }
}
