/** Holds the configurable scoring parameters (linear gap penalty). */
public final class ScoringConfig {
    public final int match;     // reward for identical characters (> 0)
    public final int mismatch;  // penalty for different characters (<= 0)
    public final int gap;       // penalty per gap position (< 0)

    public ScoringConfig(int match, int mismatch, int gap) {
        if (match <= 0)    throw new IllegalArgumentException("Match score must be positive (> 0).");
        if (mismatch > 0)  throw new IllegalArgumentException("Mismatch penalty must be zero or negative.");
        if (gap >= 0)      throw new IllegalArgumentException("Gap penalty must be negative (< 0).");
        this.match = match;
        this.mismatch = mismatch;
        this.gap = gap;
    }

    public static ScoringConfig defaults() { return new ScoringConfig(2, -1, -2); }

    /** Substitution score for aligning two characters. */
    public int score(char x, char y) { return x == y ? match : mismatch; }

    @Override public String toString() {
        return "match=+" + match + ", mismatch=" + mismatch + ", gap=" + gap;
    }
}
