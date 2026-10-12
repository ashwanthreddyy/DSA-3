import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Simple line chart (Java2D) of benchmark runtimes vs. sequence length. */
public class ChartPanel extends JPanel {
    private List<Benchmark.Point> pts = new ArrayList<>();
    private String message = "Click 'Run Benchmark' to measure runtime for growing sequence lengths.";

    public ChartPanel() { setBackground(Color.WHITE); }

    public void setPoints(List<Benchmark.Point> p) { pts = p; message = null; repaint(); }
    public void setMessage(String m) { message = m; repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Runtime vs. sequence length (random DNA, two sequences of equal length)", 20, 28);
        if (message != null || pts.isEmpty()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            g.drawString(message == null ? "" : message, 20, 60);
            return;
        }
        int L = 80, R = 40, T = 60, B = 70;
        int w = getWidth() - L - R, h = getHeight() - T - B;
        double maxY = 0; int maxX = 0;
        for (Benchmark.Point p : pts) { maxY = Math.max(maxY, Math.max(p.swMs, Math.max(p.nwMs, p.kmpMs))); maxX = Math.max(maxX, p.length); }
        maxY *= 1.1;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int k = 0; k <= 5; k++) {   // grid + y labels
            int y = T + h - k * h / 5;
            g.setColor(new Color(0, 0, 0, 30)); g.drawLine(L, y, L + w, y);
            g.setColor(Color.BLACK);
            String s = String.format("%.1f", maxY * k / 5);
            g.drawString(s, L - 8 - g.getFontMetrics().stringWidth(s), y + 4);
        }
        for (Benchmark.Point p : pts) {   // x labels
            int x = L + (int) ((long) p.length * w / maxX);
            g.setColor(new Color(0, 0, 0, 30)); g.drawLine(x, T, x, T + h);
            g.setColor(Color.BLACK);
            String s = String.valueOf(p.length);
            g.drawString(s, x - g.getFontMetrics().stringWidth(s) / 2, T + h + 18);
        }
        g.setColor(Color.BLACK); g.drawLine(L, T, L, T + h); g.drawLine(L, T + h, L + w, T + h);
        g.drawString("Sequence length (characters)", L + w / 2 - 70, T + h + 40);
        Graphics2D gr = (Graphics2D) g.create();
        gr.rotate(-Math.PI / 2, 24, T + h / 2 + 30); gr.drawString("Time (ms)", 24, T + h / 2 + 30); gr.dispose();

        Color[] cols = {new Color(21, 101, 192), new Color(46, 125, 50), new Color(230, 81, 0)};
        String[] names = {"Smith-Waterman O(mn)", "Needleman-Wunsch O(mn)", "KMP exact search O(m+n)"};
        for (int s = 0; s < 3; s++) {
            g.setColor(cols[s]); g.setStroke(new BasicStroke(2.5f));
            int px = 0, py = 0;
            for (int k = 0; k < pts.size(); k++) {
                Benchmark.Point p = pts.get(k);
                double v = s == 0 ? p.swMs : s == 1 ? p.nwMs : p.kmpMs;
                int x = L + (int) ((long) p.length * w / maxX), y = T + h - (int) (v / maxY * h);
                if (k > 0) g.drawLine(px, py, x, y);
                g.fillOval(x - 4, y - 4, 8, 8);
                px = x; py = y;
            }
            g.setStroke(new BasicStroke(1f));
            int lx = L + 20 + s * 220;
            g.fillRect(lx, 40, 14, 10); g.setColor(Color.BLACK); g.drawString(names[s], lx + 20, 50);
        }
    }
}
