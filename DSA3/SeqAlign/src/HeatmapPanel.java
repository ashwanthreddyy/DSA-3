import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/** Heatmap of the Smith-Waterman scoring matrix with the traceback path highlighted. */
public class HeatmapPanel extends JPanel {
    private static final int LEFT = 44, TOP = 70;
    private AlignmentResult r;
    private BufferedImage img;
    private int cs = 30;   // cell size in pixels

    public HeatmapPanel() { setBackground(Color.WHITE); }

    public void setResult(AlignmentResult r) {
        this.r = r;
        if (r != null) {
            int rows = r.matrix.length, cols = r.matrix[0].length;
            cs = Math.max(2, Math.min(40, 700 / Math.max(rows, cols)));
            int max = Math.max(1, r.score);
            img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) img.setRGB(j, i, color(r.matrix[i][j], max).getRGB());
        } else img = null;
        revalidate(); repaint();
    }

    private static Color color(int v, int max) {
        double t = Math.min(1.0, v / (double) max);
        return new Color((int) (255 - 242 * t), (int) (255 - 184 * t), (int) (255 - 94 * t));  // white -> dark blue
    }

    @Override public Dimension getPreferredSize() {
        if (r == null) return new Dimension(500, 200);
        return new Dimension(LEFT + r.matrix[0].length * cs + 40, TOP + r.matrix.length * cs + 40);
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        if (r == null) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.drawString("Run an alignment to see the scoring matrix.", 20, 40);
            return;
        }
        int rows = r.matrix.length, cols = r.matrix[0].length;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Scoring matrix H  (darker blue = higher score, red = traceback path)", 20, 24);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString(r.isEmpty() ? "All cells are 0: no similar region." :
                "Maximum score " + r.score + " at cell (" + r.endA + ", " + r.endB + ") - traceback starts here and stops at a 0 cell.", 20, 44);

        g.drawImage(img, LEFT, TOP, cols * cs, rows * cs, null);

        if (cs >= 14) {   // grid, labels
            g.setColor(new Color(0, 0, 0, 40));
            for (int i = 0; i <= rows; i++) g.drawLine(LEFT, TOP + i * cs, LEFT + cols * cs, TOP + i * cs);
            for (int j = 0; j <= cols; j++) g.drawLine(LEFT + j * cs, TOP, LEFT + j * cs, TOP + rows * cs);
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.min(16, cs - 2)));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 1; i < rows; i++) {
                String s = String.valueOf(r.seqA.charAt(i - 1));
                g.drawString(s, LEFT - 8 - fm.stringWidth(s), TOP + i * cs + (cs + fm.getAscent()) / 2 - 2);
            }
            for (int j = 1; j < cols; j++) {
                String s = String.valueOf(r.seqB.charAt(j - 1));
                g.drawString(s, LEFT + j * cs + (cs - fm.stringWidth(s)) / 2, TOP - 6);
            }
        }

        // traceback path
        List<int[]> path = r.path;
        g.setColor(new Color(244, 67, 54, 150));
        for (int[] p : path) g.fillRect(LEFT + p[1] * cs, TOP + p[0] * cs, cs, cs);
        if (cs >= 8 && path.size() > 1) {
            g.setColor(new Color(183, 28, 28));
            g.setStroke(new BasicStroke(2f));
            for (int k = 1; k < path.size(); k++) {
                int[] p = path.get(k - 1), q = path.get(k);
                g.drawLine(LEFT + p[1] * cs + cs / 2, TOP + p[0] * cs + cs / 2, LEFT + q[1] * cs + cs / 2, TOP + q[0] * cs + cs / 2);
            }
            g.setStroke(new BasicStroke(1f));
        }
        if (!r.isEmpty()) {
            g.setColor(Color.BLACK); g.setStroke(new BasicStroke(2.5f));
            g.drawRect(LEFT + r.endB * cs, TOP + r.endA * cs, cs, cs);
            g.setStroke(new BasicStroke(1f));
        }

        if (cs >= 22) {   // numbers
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.min(13, cs / 2)));
            FontMetrics fm = g.getFontMetrics();
            int max = Math.max(1, r.score);
            for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) {
                int v = r.matrix[i][j];
                g.setColor(v / (double) max > 0.55 ? Color.WHITE : Color.BLACK);
                String s = String.valueOf(v);
                g.drawString(s, LEFT + j * cs + (cs - fm.stringWidth(s)) / 2, TOP + i * cs + (cs + fm.getAscent()) / 2 - 2);
            }
        }
    }
}
