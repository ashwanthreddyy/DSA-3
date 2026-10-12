/** Cleaning and validation of biological sequence input. */
public final class SequenceUtil {
    public enum Type { AUTO, DNA, PROTEIN }

    public static final int MAX_LENGTH = 3000;
    private static final String DNA = "ACGTUN";
    private static final String PROTEIN = "ACDEFGHIKLMNPQRSTVWYBZXU";

    private SequenceUtil() {}

    /** Removes FASTA header lines, whitespace and digits; converts to upper case. */
    public static String clean(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String line : raw.split("\\R")) {
            line = line.trim();
            if (line.startsWith(">")) continue;
            for (char c : line.toCharArray()) {
                if (!Character.isWhitespace(c) && !Character.isDigit(c)) sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    /** Validates a cleaned sequence and returns the resolved type (DNA or PROTEIN). */
    public static Type validate(String seq, Type type, String name) {
        if (seq.isEmpty()) throw new IllegalArgumentException(name + " is empty.");
        if (seq.length() > MAX_LENGTH)
            throw new IllegalArgumentException(name + " is too long (max " + MAX_LENGTH + " characters).");
        Type t = type;
        if (type == Type.AUTO) t = allIn(seq, DNA) ? Type.DNA : Type.PROTEIN;
        String allowed = (t == Type.DNA) ? DNA : PROTEIN;
        for (int i = 0; i < seq.length(); i++) {
            if (allowed.indexOf(seq.charAt(i)) < 0)
                throw new IllegalArgumentException(name + ": invalid " + t + " character '" + seq.charAt(i)
                        + "' at position " + (i + 1) + ".");
        }
        return t;
    }

    private static boolean allIn(String s, String allowed) {
        for (int i = 0; i < s.length(); i++) if (allowed.indexOf(s.charAt(i)) < 0) return false;
        return true;
    }
}
