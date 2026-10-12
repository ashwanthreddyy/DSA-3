import javax.swing.*;
import java.awt.*;

/** Draws the aligned subsequences stacked, with colour-coded matches, mismatches and gaps. */
public class AlignmentPanel extends JPanel {
    private static final int BLOCK = 60, LABEL_COLS = 12, END_COLS = 8, PAD = 20, LINE = 26;
    private static final Color MATCH = new Color(200, 230, 201), MISMATCH = new Color(255, 224, 178), GAP = new Color(255, 205, 210);
    private final Font mono = new Font(Font.MONOSPACED, Font.BOLD, 16);
    private final Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private AlignmentResult r;

    public AlignmentPanel() { setBackground(Color.WHITE); }

    public void setResult(AlignmentResult r) { this.r = r; revalidate(); repaint(); }

    @Override public Dimension getPreferredSize() {
        int cw = getFontMetrics(mono).charWidth('M');
        int blocks = (r == null || r.isEmpty()) ? 0 : (r.length() + BLOCK - 1) / BLOCK;
        return new Dimension(Math.max(700, PAD * 2 + (LABEL_COLS + BLOCK + END_COLS) * cw),
                             Math.max(300, 190 + blocks * (3 * LINE + 12)));
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        int y = PAD + 16;
        if (r == null) {
            g.setFont(sans.deriveFont(Font.BOLD, 16f));
            g.drawString("Enter two sequences and click 'Align' to see the best local alignment.", PAD, y);
            return;
        }
        g.setFont(sans.deriveFont(Font.BOLD, 20f));
        g.drawString("Optimal local alignment score: " + r.score, PAD, y);
        g.setFont(sans);
        y += 26; g.drawString("Scoring: " + r.cfg, PAD, y);
        if (r.isEmpty()) {
            y += 26; g.drawString("No similar region found with the current scoring parameters.", PAD, y);
            return;
        }
        y += 22; g.drawString(String.format("Region in Seq A: %d-%d    |    Region in Seq B: %d-%d", r.startA, r.endA, r.startB, r.endB), PAD, y);
        y += 22; g.drawString(String.format("Length: %d    Matches: %d    Mismatches: %d    Gaps: %d    Identity: %.1f%%",
                r.length(), r.matches, r.mismatches, r.gaps, r.identity()), PAD, y);
        y += 44;

        g.setFont(mono);
        int cw = g.getFontMetrics().charWidth('M');
        int x0 = PAD + LABEL_COLS * cw;
        int pa = r.startA, pb = r.startB;
        for (int k = 0; k < r.length(); k += BLOCK) {
            int e = Math.min(r.length(), k + BLOCK);
            int yA = y, yM = y + LINE, yB = y + 2 * LINE, na = 0, nb = 0;
            for (int c = k; c < e; c++) {
                int x = x0 + (c - k) * cw;
                char ca = r.alignedA.charAt(c), cb = r.alignedB.charAt(c), cm = r.midline.charAt(c);
                g.setColor(cm == '|' ? MATCH : cm == ':' ? MISMATCH : GAP);
                g.fillRect(x, yA - 18, cw, 2 * LINE + 24);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(ca), x, yA);
                g.drawString(String.valueOf(cm == ' ' ? ' ' : cm), x, yM);
                g.drawString(String.valueOf(cb), x, yB);
                if (ca != '-') na++;
                if (cb != '-') nb++;
            }
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format("Seq A %5d", pa), PAD, yA);
            g.drawString(String.format("Seq B %5d", pb), PAD, yB);
            int xe = x0 + (e - k + 1) * cw;
            g.drawString(String.valueOf(pa + na - 1), xe, yA);
            g.drawString(String.valueOf(pb + nb - 1), xe, yB);
            pa += na; pb += nb;
            y += 3 * LINE + 12;
        }
        // legend
        y += 4;
        g.setFont(sans);
        int lx = PAD;
        lx = legend(g, lx, y, MATCH, "match ( | )");
        lx = legend(g, lx, y, MISMATCH, "mismatch ( : )");
        legend(g, lx, y, GAP, "gap ( - )");
    }

    private int legend(Graphics2D g, int x, int y, Color c, String text) {
        g.setColor(c); g.fillRect(x, y - 12, 16, 16);
        g.setColor(Color.BLACK); g.drawRect(x, y - 12, 16, 16);
        g.drawString(text, x + 22, y);
        return x + 22 + g.getFontMetrics().stringWidth(text) + 24;
    }
}
