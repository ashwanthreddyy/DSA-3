import javax.swing.*;

/**
 * Entry point.
 *   java Main                                  -> GUI
 *   java Main --cli SEQ_A SEQ_B [match mismatch gap]  -> console output
 */
public class Main {
    public static void main(String[] args) {
        if (args.length >= 3 && args[0].equals("--cli")) { cli(args); return; }
        if (args.length == 2 && args[0].equals("--screenshots")) { ScreenshotTool.run(args[1]); return; }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }

    private static void cli(String[] args) {
        try {
            String a = SequenceUtil.clean(args[1]), b = SequenceUtil.clean(args[2]);
            SequenceUtil.validate(a, SequenceUtil.Type.AUTO, "Sequence A");
            SequenceUtil.validate(b, SequenceUtil.Type.AUTO, "Sequence B");
            ScoringConfig cfg = args.length >= 6
                    ? new ScoringConfig(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]))
                    : ScoringConfig.defaults();
            AlignmentResult r = SmithWaterman.align(a, b, cfg);
            System.out.println(r.toText(60));
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
