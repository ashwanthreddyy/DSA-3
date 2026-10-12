/** Plain-Java test runner (no external libraries). Run: java Tests */
public class Tests {
    static int pass = 0, fail = 0;

    static void check(String name, Object expected, Object actual) {
        boolean ok = String.valueOf(expected).equals(String.valueOf(actual));
        System.out.printf("%s  %-40s expected=%-14s actual=%s%n", ok ? "PASS" : "FAIL", name, expected, actual);
        if (ok) pass++; else fail++;
    }

    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) {
        ScoringConfig d = ScoringConfig.defaults();          // +2 / -1 / -2
        AlignmentResult r;

        // T1 identical sequences
        r = SmithWaterman.align("ACGTACGT", "ACGTACGT", d);
        check("T1 identical: score", 16, r.score);
        check("T1 identical: aligned region", "ACGTACGT", r.alignedA);

        // T2 no similarity
        r = SmithWaterman.align("AAAA", "TTTT", d);
        check("T2 no similarity: score", 0, r.score);
        check("T2 no similarity: empty result", true, r.isEmpty());

        // T3 partial overlap (shared region ACGGT)
        r = SmithWaterman.align("TTACGGTAAA", "GGACGGTCCC", d);
        check("T3 partial overlap: score", 10, r.score);
        check("T3 partial overlap: region", "ACGGT", r.alignedA);
        check("T3 partial overlap: pos in A", "3-7", r.startA + "-" + r.endA);
        check("T3 partial overlap: pos in B", "3-7", r.startB + "-" + r.endB);

        // T4 alignment containing a gap
        r = SmithWaterman.align("ACGTTTGCA", "ACGTTGCA", d);
        check("T4 gap: score (8 matches - 1 gap)", 14, r.score);
        check("T4 gap: number of gaps", 1, r.gaps);
        check("T4 gap: A without gaps", "ACGTTTGCA", r.alignedA.replace("-", ""));
        check("T4 gap: B without gaps", "ACGTTGCA", r.alignedB.replace("-", ""));

        // T5 textbook example (Wikipedia): match +3, mismatch -3, gap -2
        ScoringConfig wiki = new ScoringConfig(3, -3, -2);
        r = SmithWaterman.align("TGTTACGG", "GGTTGACTA", wiki);
        check("T5 textbook: score", 13, r.score);
        check("T5 textbook: aligned A", "GTT-AC", r.alignedA);
        check("T5 textbook: aligned B", "GTTGAC", r.alignedB);

        // T6 same sequences, harsher gap penalty -> different optimal alignment
        r = SmithWaterman.align("TGTTACGG", "GGTTGACTA", new ScoringConfig(3, -3, -10));
        check("T6 gap=-10: score", 9, r.score);
        check("T6 gap=-10: region", "GTT", r.alignedA);

        // T7 protein sequences, one inside the other
        r = SmithWaterman.align("MKTAYIAKQR", "TAYIAK", d);
        check("T7 protein: score", 12, r.score);
        check("T7 protein: pos in A", "3-8", r.startA + "-" + r.endA);
        check("T7 protein: pos in B", "1-6", r.startB + "-" + r.endB);

        // T8 symmetry: score(A,B) == score(B,A)
        String s1 = "GATTACAGGCTTAAGCTTCGATCGG", s2 = "CCGTAGCTTAAGCTTAGGATCGGAT";
        check("T8 symmetry of local score", SmithWaterman.align(s1, s2, d).score, SmithWaterman.align(s2, s1, d).score);

        // T9 input handling
        check("T9 clean FASTA/lowercase/spaces", "ACGT", SequenceUtil.clean(">seq1\nacg t\n"));
        check("T9 invalid DNA char rejected", true,
                throwsIAE(() -> SequenceUtil.validate("ACGZ", SequenceUtil.Type.DNA, "Seq")));
        check("T9 empty input rejected", true,
                throwsIAE(() -> SequenceUtil.validate("", SequenceUtil.Type.AUTO, "Seq")));
        check("T9 invalid scoring rejected (match=0)", true, throwsIAE(() -> new ScoringConfig(0, -1, -2)));

        // T10 baselines
        check("T10 KMP occurrences", "[0, 4]", KMP.search("ACGTACG", "ACG"));
        check("T10 Needleman-Wunsch identical", 8, NeedlemanWunsch.score("ACGT", "ACGT", d));

        System.out.printf("%n%d passed, %d failed%n", pass, fail);
        System.exit(fail == 0 ? 0 : 1);
    }
}
