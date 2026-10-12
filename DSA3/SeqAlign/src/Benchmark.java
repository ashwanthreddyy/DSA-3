import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Runtime benchmark of Smith-Waterman, Needleman-Wunsch and KMP on random DNA of growing length. */
public final class Benchmark {
    public static final class Point {
        public final int length;
        public final double swMs, nwMs, kmpMs;
        Point(int length, double swMs, double nwMs, double kmpMs) {
            this.length = length; this.swMs = swMs; this.nwMs = nwMs; this.kmpMs = kmpMs;
        }
    }

    public interface Progress { void update(int done, int total); }

    private Benchmark() {}

    private static String randomDna(Random r, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append("ACGT".charAt(r.nextInt(4)));
        return sb.toString();
    }

    public static List<Point> run(int[] lengths, int reps, ScoringConfig cfg, Progress progress) {
        Random rnd = new Random(42);
        String w1 = randomDna(rnd, 800), w2 = randomDna(rnd, 800);      // JIT warm-up
        for (int i = 0; i < 12; i++) {
            SmithWaterman.align(w1, w2, cfg); NeedlemanWunsch.score(w1, w2, cfg); KMP.search(w1, w2.substring(0, 20));
        }
        List<Point> out = new ArrayList<>();
        int done = 0;
        for (int len : lengths) {
            String a = randomDna(rnd, len), b = randomDna(rnd, len);
            String pat = a.substring(len / 2, len / 2 + 20);
            SmithWaterman.align(a, b, cfg); NeedlemanWunsch.score(a, b, cfg);   // untimed pass for this size
            long sw = Long.MAX_VALUE, nw = Long.MAX_VALUE, kmp = Long.MAX_VALUE;   // best-of-N is robust to GC/JIT noise
            for (int r = 0; r < reps; r++) {
                long t0 = System.nanoTime(); SmithWaterman.align(a, b, cfg);        sw = Math.min(sw, System.nanoTime() - t0);
                t0 = System.nanoTime();      NeedlemanWunsch.score(a, b, cfg);      nw = Math.min(nw, System.nanoTime() - t0);
                t0 = System.nanoTime();      for (int k = 0; k < 200; k++) KMP.search(b, pat); kmp = Math.min(kmp, (System.nanoTime() - t0) / 200);
            }
            out.add(new Point(len, sw / 1e6, nw / 1e6, kmp / 1e6));
            if (progress != null) progress.update(++done, lengths.length);
        }
        return out;
    }
}
