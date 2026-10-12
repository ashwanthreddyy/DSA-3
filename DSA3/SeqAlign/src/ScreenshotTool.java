import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/** Developer utility: renders each tab to a PNG (used to create the screenshots for the report). */
public class ScreenshotTool {
    public static void run(String dir) {
        try {
            new File(dir).mkdirs();
            final MainFrame[] holder = new MainFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                holder[0] = new MainFrame();
                holder[0].setVisible(true);
                holder[0].runAlignment();
            });
            MainFrame f = holder[0];
            List<Benchmark.Point> pts = Benchmark.run(new int[]{100, 200, 400, 600, 800, 1000, 1500, 2000}, 15, ScoringConfig.defaults(), null);
            System.out.println("length,SW_ms,NW_ms,KMP_ms");
            for (Benchmark.Point p : pts) System.out.printf("%d,%.2f,%.2f,%.4f%n", p.length, p.swMs, p.nwMs, p.kmpMs);
            String[] names = {"1_alignment", "2_heatmap", "3_comparison", "4_benchmark"};
            for (int t = 0; t < 4; t++) {
                final int idx = t;
                SwingUtilities.invokeAndWait(() -> { f.getTabs().setSelectedIndex(idx); if (idx == 3) f.getChart().setPoints(pts); f.validate(); });
                Thread.sleep(400);
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        Container cp = f.getContentPane();
                        BufferedImage img = new BufferedImage(cp.getWidth(), cp.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = img.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, img.getWidth(), img.getHeight());
                        cp.paint(g); g.dispose();
                        ImageIO.write(img, "png", new File(dir, names[idx] + ".png"));
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
            }
            System.exit(0);
        } catch (Exception e) { e.printStackTrace(); System.exit(1); }
    }
}
