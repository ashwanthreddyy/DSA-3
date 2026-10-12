import java.util.ArrayList;
import java.util.List;

/** Knuth-Morris-Pratt exact string matching (O(n+m)), used as a baseline for comparison. */
public final class KMP {
    private KMP() {}

    public static int[] failure(String p) {
        int[] f = new int[p.length()];
        for (int i = 1, k = 0; i < p.length(); i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = f[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            f[i] = k;
        }
        return f;
    }

    /** Returns the 0-based start indices of every exact occurrence of p in t. */
    public static List<Integer> search(String t, String p) {
        List<Integer> res = new ArrayList<>();
        if (p.isEmpty() || p.length() > t.length()) return res;
        int[] f = failure(p);
        for (int i = 0, k = 0; i < t.length(); i++) {
            while (k > 0 && t.charAt(i) != p.charAt(k)) k = f[k - 1];
            if (t.charAt(i) == p.charAt(k)) k++;
            if (k == p.length()) { res.add(i - k + 1); k = f[k - 1]; }
        }
        return res;
    }
}
