import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/** Main Swing window: inputs, configurable scoring parameters and result tabs. */
public class MainFrame extends JFrame {
    static final String DNA_A = "GATTACAGGCTTAAGCTTCGATCGGATCCATGCAAGTCCTAGGATTCA";
    static final String DNA_B = "CCGTAGCTTAAGCTTAGGATCGGATCCATGCTTGACCGTA";
    static final String PROT_A = "VLSPADKTNVKAAWGKVGAHAGEYGAEALERMFLSFPTTKTYFPHFDLSHGSAQVKGHGKKVADALTNAVAHVDDMPNALSALSDLHAHKLRVDPVNFKLLSHCLLVTLAAHLPAEFTPAVHASLDKFLASVSTVLTSKYR";
    static final String PROT_B = "VLSEGEWQLVLHVWAKVEADVAGHGQDILIRLFKSHPETLEKFDRFKHLKTEAEMKASEDLKKHGVTVLTALGAILKKKGHHEAELKPLAQSHATKHKIPIKYLEFISEAIIHVLHSRHPGDFGADAQGAMNKALELFRKDIAAKYKELGYQG";

    private final JTextArea taA = new JTextArea(3, 50), taB = new JTextArea(3, 50);
    private final JComboBox<SequenceUtil.Type> typeBox = new JComboBox<>(SequenceUtil.Type.values());
    private final JSpinner spMatch = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JSpinner spMismatch = new JSpinner(new SpinnerNumberModel(-1, -100, 0, 1));
    private final JSpinner spGap = new JSpinner(new SpinnerNumberModel(-2, -100, -1, 1));
    private final AlignmentPanel alignPanel = new AlignmentPanel();
    private final HeatmapPanel heatPanel = new HeatmapPanel();
    private final JTextArea report = new JTextArea();
    private final ChartPanel chart = new ChartPanel();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel status = new JLabel(" Ready.");
    private final JButton benchBtn = new JButton("Run Benchmark");
    private AlignmentResult last;
    private String lastReport = "";

    public MainFrame() {
        super("SeqMatch - Local Sequence Alignment (Smith-Waterman)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        taA.setLineWrap(true); taB.setLineWrap(true);
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        taA.setFont(mono); taB.setFont(mono);
        taA.setText(DNA_A); taB.setText(DNA_B);

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4); c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; top.add(new JLabel("Sequence A:"), c);
        c.gridx = 1; c.weightx = 1; c.gridwidth = 8; top.add(new JScrollPane(taA), c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.gridwidth = 1; top.add(new JLabel("Sequence B:"), c);
        c.gridx = 1; c.weightx = 1; c.gridwidth = 8; top.add(new JScrollPane(taB), c);

        JPanel params = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        params.add(new JLabel("Type:")); params.add(typeBox);
        params.add(new JLabel("Match:")); params.add(spMatch);
        params.add(new JLabel("Mismatch:")); params.add(spMismatch);
        params.add(new JLabel("Gap:")); params.add(spGap);
        JButton alignBtn = new JButton("Align");
        alignBtn.setFont(alignBtn.getFont().deriveFont(Font.BOLD));
        JButton dnaBtn = new JButton("DNA Sample"), protBtn = new JButton("Protein Sample");
        JButton clearBtn = new JButton("Clear"), saveBtn = new JButton("Save Result");
        params.add(alignBtn); params.add(dnaBtn); params.add(protBtn); params.add(clearBtn); params.add(saveBtn);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 9; top.add(params, c);
        add(top, BorderLayout.NORTH);

        report.setEditable(false); report.setFont(mono); report.setMargin(new Insets(10, 10, 10, 10));
        report.setText("Run an alignment to see how Smith-Waterman compares with Needleman-Wunsch and KMP.");
        JPanel benchPanel = new JPanel(new BorderLayout());
        JPanel benchTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        benchTop.add(benchBtn);
        benchTop.add(new JLabel("Uses the current scoring parameters; lengths 100 to 2000."));
        benchPanel.add(benchTop, BorderLayout.NORTH); benchPanel.add(chart, BorderLayout.CENTER);

        tabs.addTab("Alignment", new JScrollPane(alignPanel));
        tabs.addTab("Score Matrix Heatmap", new JScrollPane(heatPanel));
        tabs.addTab("Algorithm Comparison", new JScrollPane(report));
        tabs.addTab("Benchmark", benchPanel);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        alignBtn.addActionListener(e -> runAlignment());
        dnaBtn.addActionListener(e -> { taA.setText(DNA_A); taB.setText(DNA_B); typeBox.setSelectedItem(SequenceUtil.Type.AUTO); });
        protBtn.addActionListener(e -> { taA.setText(PROT_A); taB.setText(PROT_B); typeBox.setSelectedItem(SequenceUtil.Type.AUTO); });
        clearBtn.addActionListener(e -> { taA.setText(""); taB.setText(""); last = null; alignPanel.setResult(null); heatPanel.setResult(null);
            report.setText(""); status.setText(" Cleared."); });
        saveBtn.addActionListener(e -> saveResult());
        benchBtn.addActionListener(e -> runBenchmark());
        setSize(1150, 820);
        setLocationRelativeTo(null);
    }

    /** Validates the inputs, runs Smith-Waterman and refreshes every tab. */
    public void runAlignment() {
        try {
            String a = SequenceUtil.clean(taA.getText()), b = SequenceUtil.clean(taB.getText());
            SequenceUtil.Type type = (SequenceUtil.Type) typeBox.getSelectedItem();
            SequenceUtil.Type ta = SequenceUtil.validate(a, type, "Sequence A");
            SequenceUtil.Type tb = SequenceUtil.validate(b, type, "Sequence B");
            ScoringConfig cfg = new ScoringConfig((Integer) spMatch.getValue(), (Integer) spMismatch.getValue(), (Integer) spGap.getValue());
            long t0 = System.nanoTime();
            AlignmentResult res = SmithWaterman.align(a, b, cfg);
            double ms = (System.nanoTime() - t0) / 1e6;
            last = res;
            lastReport = AnalysisReport.build(a, b, cfg, res);
            alignPanel.setResult(res); heatPanel.setResult(res);
            report.setText(lastReport); report.setCaretPosition(0);
            status.setText(String.format(" Done: score %d | Seq A (%s, %d) vs Seq B (%s, %d) | %.2f ms", res.score, ta, a.length(), tb, b.length(), ms));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void runBenchmark() {
        final ScoringConfig cfg;
        try {
            cfg = new ScoringConfig((Integer) spMatch.getValue(), (Integer) spMismatch.getValue(), (Integer) spGap.getValue());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid scoring", JOptionPane.WARNING_MESSAGE);
            return;
        }
        benchBtn.setEnabled(false);
        chart.setMessage("Running benchmark...");
        new SwingWorker<List<Benchmark.Point>, Integer>() {
            @Override protected List<Benchmark.Point> doInBackground() {
                return Benchmark.run(new int[]{100, 200, 400, 600, 800, 1000, 1500, 2000}, 15, cfg, (d, t) -> publish(d));
            }
            @Override protected void process(List<Integer> chunks) { status.setText(" Benchmark: " + chunks.get(chunks.size() - 1) + "/8 lengths done"); }
            @Override protected void done() {
                try { chart.setPoints(get()); status.setText(" Benchmark finished."); }
                catch (Exception ex) { chart.setMessage("Benchmark failed: " + ex.getMessage()); }
                benchBtn.setEnabled(true);
            }
        }.execute();
    }

    private void saveResult() {
        if (last == null) { JOptionPane.showMessageDialog(this, "Run an alignment first.", "Nothing to save", JOptionPane.INFORMATION_MESSAGE); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("alignment_result.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fc.getSelectedFile().toPath(), (last.toText(60) + "\n" + lastReport).getBytes(StandardCharsets.UTF_8));
                status.setText(" Saved to " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // accessors used by ScreenshotTool
    public JTabbedPane getTabs() { return tabs; }
    public ChartPanel getChart() { return chart; }
}
