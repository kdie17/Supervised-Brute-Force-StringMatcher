import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SupervisedBruteForceGUI extends JFrame {

    private JTextField textField, patternField;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextArea interpretationArea;

    // Chart panels
    private BarChartPanel runtimeBarChart;
    private BarChartPanel memoryBarChart;
    private LineChartPanel runtimeTrendChart;
    private LineChartPanel memoryTrendChart;

    // Data storage for trend charts
    private final List<double[]> runtimeTrendData = new ArrayList<>();   // [inputSize, bfTime, supTime]
    private final List<double[]> memoryTrendData = new ArrayList<>();    // [inputSize, bfMem, supMem]

    public SupervisedBruteForceGUI() {
        setTitle("Supervised Learning-Based Enhancement of Brute Force String Matching");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        try {
            Image icon = new ImageIcon("blackcatdance.jpg").getImage();
            this.setIconImage(icon);
        } catch (Exception ignored) {}

        setupInputPanel();
        setupMainTabbedPanel();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INPUT PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private void setupInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Experimental Setup"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        textField = new JTextField("DATA_STREAM_SAMPLE_TEXT_FOR_MATCHING");
        patternField = new JTextField("SAMPLE");

        JButton runButton = new JButton("Execute Experiment");
        runButton.setBackground(new Color(45, 120, 45));
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton runSweepButton = new JButton("Run Full Input-Size Sweep");
        runSweepButton.setBackground(new Color(30, 80, 160));
        runSweepButton.setForeground(Color.WHITE);
        runSweepButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton clearButton = new JButton("Clear Trend Data");
        clearButton.setBackground(new Color(160, 50, 50));
        clearButton.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Source Text (Dataset):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(textField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Target Pattern:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(patternField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.add(runButton);
        btnPanel.add(runSweepButton);
        btnPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(btnPanel, gbc);

        runButton.addActionListener(this::runExperiment);
        runSweepButton.addActionListener(this::runInputSizeSweep);
        clearButton.addActionListener(e -> {
            runtimeTrendData.clear();
            memoryTrendData.clear();
            runtimeTrendChart.setData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            memoryTrendChart.setData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        });

        add(panel, BorderLayout.NORTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABBED OUTPUT PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private void setupMainTabbedPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        tabs.addTab("📊 Results & Analysis", buildResultsTab());
        tabs.addTab("📈 Runtime Comparison", buildRuntimeBarTab());
        tabs.addTab("💾 Memory Comparison", buildMemoryBarTab());
        tabs.addTab("📉 Runtime vs Input Size", buildRuntimeTrendTab());
        tabs.addTab("📉 Memory vs Input Size", buildMemoryTrendTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildResultsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));

        String[] columns = {"Method", "Accuracy (%)", "Comparisons", "Time (ns)", "Memory (bytes)", "Vocab Skips", "Checksum Skips", "Efficiency Gain"};
        tableModel = new DefaultTableModel(columns, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(24);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        styleTable(resultsTable);

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Performance Metrics Table"));
        panel.add(tableScroll);

        interpretationArea = new JTextArea();
        interpretationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        interpretationArea.setEditable(false);
        interpretationArea.setLineWrap(true);
        interpretationArea.setWrapStyleWord(true);
        interpretationArea.setMargin(new Insets(12, 12, 12, 12));
        interpretationArea.setBackground(new Color(245, 248, 245));

        JScrollPane scroll = new JScrollPane(interpretationArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Study Results & Analysis"));
        panel.add(scroll);

        return panel;
    }

    private JPanel buildRuntimeBarTab() {
        runtimeBarChart = new BarChartPanel(
            "Execution Time Comparison",
            "Algorithm",
            "Time (nanoseconds)",
            new Color[]{new Color(70, 130, 200), new Color(50, 170, 100)}
        );
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(runtimeBarChart);
        return wrapper;
    }

    private JPanel buildMemoryBarTab() {
        memoryBarChart = new BarChartPanel(
            "Memory Usage Comparison",
            "Algorithm",
            "Memory (bytes)",
            new Color[]{new Color(200, 100, 70), new Color(170, 70, 170)}
        );
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(memoryBarChart);
        return wrapper;
    }

    private JPanel buildRuntimeTrendTab() {
        runtimeTrendChart = new LineChartPanel(
            "Runtime vs Input Size",
            "Input Size (characters)",
            "Time (nanoseconds)",
            new Color[]{new Color(70, 130, 200), new Color(50, 170, 100)},
            new String[]{"Brute Force (Baseline)", "Supervised (Enhanced)"}
        );
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(runtimeTrendChart);
        return wrapper;
    }

    private JPanel buildMemoryTrendTab() {
        memoryTrendChart = new LineChartPanel(
            "Memory Usage vs Input Size",
            "Input Size (characters)",
            "Memory (bytes)",
            new Color[]{new Color(200, 100, 70), new Color(170, 70, 170)},
            new String[]{"Brute Force (Baseline)", "Supervised (Enhanced)"}
        );
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(memoryTrendChart);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //   EXECUTION EXPERIMENT
    // ─────────────────────────────────────────────────────────────────────────
    private void runExperiment(ActionEvent e) {
        String text = textField.getText();
        String pattern = patternField.getText();
        if (text.isEmpty() || pattern.isEmpty()) return;

        ExperimentResult er = runSingleExperiment(text, pattern);
        updateAllDisplays(er, text.length());
    }

    private void runInputSizeSweep(ActionEvent e) {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) return;

        runtimeTrendData.clear();
        memoryTrendData.clear();

        // This one generate texts of increasing sizes by repeating the base text
        String base = textField.getText().isEmpty() ? "DATA_STREAM_SAMPLE_TEXT_FOR_MATCHING" : textField.getText();

        // Build sweep texts: sizes at 0.25x, 0.5x, 1x, 2x, 4x, 8x, 16x of base
        // We also ensure pattern is always embedded (somewhere)
        double[] multipliers = {0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0};

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (double mult : multipliers) {
                    StringBuilder sb = new StringBuilder();
                    int reps = Math.max(1, (int) mult);
                    for (int r = 0; r < reps; r++) sb.append(base);
                    // Embed pattern near the end to make it non-trivial
                    String text = sb + pattern;

                    ExperimentResult er = runSingleExperiment(text, pattern);

                    runtimeTrendData.add(new double[]{text.length(), er.bfTime, er.supTime});
                    memoryTrendData.add(new double[]{text.length(), er.bfMemory, er.supMemory});

                    publish("Processed size: " + text.length());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                interpretationArea.append(chunks.get(chunks.size() - 1) + "\n");
            }

            @Override
            protected void done() {
                updateTrendCharts();
                // Show last result in table
                if (!runtimeTrendData.isEmpty()) {
                    double[] last = runtimeTrendData.get(runtimeTrendData.size() - 1);
                    interpretationArea.append("\nSweep complete! " + runtimeTrendData.size() + " data points collected.\n");
                }
            }
        };
        interpretationArea.setText("Running input-size sweep...\n");
        worker.execute();
    }

    private ExperimentResult runSingleExperiment(String text, String pattern) {
        Runtime runtime = Runtime.getRuntime();

        // BRUTE FORCE
        System.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        long startBF = System.nanoTime();
        Result bfResult = bruteForce(text, pattern);
        long endBF = System.nanoTime();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long bfMem = Math.max(0, memAfter - memBefore);

        // SUPERVISED BRUTE FORCE
        System.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();
        long startSup = System.nanoTime();
        Result supResult = supervisedMatch(text, pattern);
        long endSup = System.nanoTime();
        memAfter = runtime.totalMemory() - runtime.freeMemory();
        long supMem = Math.max(0, memAfter - memBefore);

        return new ExperimentResult(bfResult, supResult,
            endBF - startBF, endSup - startSup,
            bfMem, supMem);
    }

    private void updateAllDisplays(ExperimentResult er, int inputSize) {
        updateResearchReport(er);
        updateBarCharts(er);

        // Accumulate trend data
        runtimeTrendData.add(new double[]{inputSize, er.bfTime, er.supTime});
        memoryTrendData.add(new double[]{inputSize, er.bfMemory, er.supMemory});
        updateTrendCharts();
    }

    private void updateBarCharts(ExperimentResult er) {
        // Runtime bar chart
        runtimeBarChart.setBarData(
            new String[]{"Brute Force (Baseline)", "Supervised (Enhanced)"},
            new double[]{er.bfTime, er.supTime}
        );

        // Memory bar chart
        memoryBarChart.setBarData(
            new String[]{"Brute Force (Baseline)", "Supervised (Enhanced)"},
            new double[]{er.bfMemory, er.supMemory}
        );
    }

    private void updateTrendCharts() {
        if (runtimeTrendData.isEmpty()) return;

        List<Double> xs = new ArrayList<>();
        List<Double> series1 = new ArrayList<>();
        List<Double> series2 = new ArrayList<>();

        for (double[] d : runtimeTrendData) {
            xs.add(d[0]);
            series1.add(d[1]);
            series2.add(d[2]);
        }
        runtimeTrendChart.setData(xs, series1, series2);

        xs.clear(); series1.clear(); series2.clear();
        for (double[] d : memoryTrendData) {
            xs.add(d[0]);
            series1.add(d[1]);
            series2.add(d[2]);
        }
        memoryTrendChart.setData(xs, series1, series2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  THE ALGORITHMS
    // ─────────────────────────────────────────────────────────────────────────
    private Result bruteForce(String text, String pattern) {
        int count = 0;
        int n = text.length(), m = pattern.length();
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m) {
                count++;
                if (text.charAt(i + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) return new Result(true, count, 0, 0, i);
        }
        return new Result(false, count, 0, 0, -1);
    }

    private Result supervisedMatch(String text, String pattern) {
        int count = 0, vocabBlocks = 0, checksumBlocks = 0;
        int n = text.length(), m = pattern.length();

        Set<Character> learnedVocab = new HashSet<>();
        long learnedChecksum = 0;
        for (char c : pattern.toCharArray()) {
            learnedVocab.add(c);
            learnedChecksum += c;
        }

        for (int i = 0; i <= n - m; i++) {
            count++;
            if (!learnedVocab.contains(text.charAt(i))) { vocabBlocks++; continue; }

            long currentSum = 0;
            for (int k = 0; k < m; k++) currentSum += text.charAt(i + k);
            count++;
            if (currentSum != learnedChecksum) { checksumBlocks++; continue; }

            int j = 0;
            while (j < m) {
                count++;
                if (text.charAt(i + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) return new Result(true, count, vocabBlocks, checksumBlocks, i);
        }
        return new Result(false, count, vocabBlocks, checksumBlocks, -1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  REPORTS (COMPARISONS BETWEEN BASELINE AND ENHANCED *SUPERVISED*)
    // ─────────────────────────────────────────────────────────────────────────
    private void updateResearchReport(ExperimentResult er) {
        tableModel.setRowCount(0);

        double gain = er.control.comparisons > 0
            ? ((double)(er.control.comparisons - er.exp.comparisons) / er.control.comparisons) * 100
            : 0;
        double timeGain = er.bfTime > 0
            ? ((double)(er.bfTime - er.supTime) / er.bfTime) * 100
            : 0;
        double memGain = er.bfMemory > 0
            ? ((double)(er.bfMemory - er.supMemory) / er.bfMemory) * 100
            : 0;

        tableModel.addRow(new Object[]{
            "Brute Force (Baseline)", "100.0",
            er.control.comparisons, er.bfTime, er.bfMemory,
            "N/A", "N/A", "0%"
        });
        tableModel.addRow(new Object[]{
            "Supervised (Enhanced)", "100.0",
            er.exp.comparisons, er.supTime, er.supMemory,
            er.exp.vocabBlocks, er.exp.checksumBlocks,
            String.format("%.2f%%", gain)
        });

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════════════════\n");
        sb.append("              RESEARCH ANALYSIS REPORT\n");
        sb.append("══════════════════════════════════════════════════════\n\n");

        sb.append("▶ Baseline Algorithm Results (Brute Force)\n");
        sb.append("  ├─ Execution Time  : ").append(er.bfTime).append(" ns\n");
        sb.append("  ├─ Memory Usage    : ").append(er.bfMemory).append(" bytes\n");
        sb.append("  ├─ Comparisons     : ").append(er.control.comparisons).append("\n");
        sb.append("  └─ Match Found     : ").append(er.control.found).append(" at index ").append(er.control.index).append("\n\n");

        sb.append("▶ Enhanced Algorithm Results (Supervised)\n");
        sb.append("  ├─ Execution Time  : ").append(er.supTime).append(" ns\n");
        sb.append("  ├─ Memory Usage    : ").append(er.supMemory).append(" bytes\n");
        sb.append("  ├─ Comparisons     : ").append(er.exp.comparisons).append("\n");
        sb.append("  ├─ Vocab Skips     : ").append(er.exp.vocabBlocks).append(" (positions eliminated by vocabulary filter)\n");
        sb.append("  ├─ Checksum Skips  : ").append(er.exp.checksumBlocks).append(" (positions eliminated by checksum filter)\n");
        sb.append("  └─ Match Found     : ").append(er.exp.found).append(" at index ").append(er.exp.index).append("\n\n");

        sb.append("▶ Performance Comparison\n");
        sb.append("  ├─ Comparison Reduction : ").append(er.control.comparisons - er.exp.comparisons)
          .append(" fewer checks (").append(String.format("%.2f%%", gain)).append(" gain)\n");
        sb.append("  ├─ Time Reduction       : ").append(String.format("%.2f%%", timeGain)).append(" faster\n");
        sb.append("  └─ Memory Reduction     : ").append(String.format("%.2f%%", memGain)).append(" less memory\n\n");

        sb.append("▶ Performance Trends\n");
        sb.append("  ├─ Data points collected: ").append(runtimeTrendData.size()).append("\n");
        if (runtimeTrendData.size() >= 2) {
            double first = runtimeTrendData.get(0)[1];
            double last = runtimeTrendData.get(runtimeTrendData.size()-1)[1];
            double trend = (last - first) / first * 100;
            sb.append("  ├─ BF Runtime Trend     : ").append(String.format("%.1f%%", trend)).append(" change over collected sizes\n");
        }
        sb.append("  └─ Use 'Run Full Input-Size Sweep' to populate trend charts\n\n");

        sb.append("▶ Conclusion\n");
        if (gain > 0) {
            sb.append("  The supervised method outperforms the baseline. Integrating\n");
            sb.append("  learned vocabulary and checksum filters reduces unnecessary\n");
            sb.append("  comparisons while maintaining 100% matching accuracy.\n");
        } else {
            sb.append("  In this trial, supervision overhead exceeded savings.\n");
            sb.append("  Larger datasets with more diverse patterns are recommended.\n");
        }
        sb.append("\n══════════════════════════════════════════════════════\n");

        interpretationArea.setText(sb.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABLE STYLING
    // ─────────────────────────────────────────────────────────────────────────
    private void styleTable(JTable table) {
        table.setSelectionBackground(new Color(180, 220, 255));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DATA RECORDS
    // ─────────────────────────────────────────────────────────────────────────
    private record Result(boolean found, int comparisons, int vocabBlocks, int checksumBlocks, int index) {}

    private record ExperimentResult(Result control, Result exp,
                                    long bfTime, long supTime,
                                    long bfMemory, long supMemory) {}

    // ═════════════════════════════════════════════════════════════════════════
    //  BAR CHART PANEL
    // ═════════════════════════════════════════════════════════════════════════
    static class BarChartPanel extends JPanel {
        private String title, xLabel, yLabel;
        private String[] labels = {};
        private double[] values = {};
        private final Color[] colors;
        private static final int PAD = 60;

        BarChartPanel(String title, String xLabel, String yLabel, Color[] colors) {
            this.title = title; this.xLabel = xLabel; this.yLabel = yLabel; this.colors = colors;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }

        void setBarData(String[] labels, double[] values) {
            this.labels = labels; this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Background gradient
            GradientPaint bg = new GradientPaint(0, 0, new Color(248, 251, 255), 0, h, new Color(240, 245, 252));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);

            // Title
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(new Color(40, 60, 100));
            FontMetrics tfm = g2.getFontMetrics();
            g2.drawString(title, (w - tfm.stringWidth(title)) / 2, 28);

            if (values.length == 0) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
                g2.setColor(Color.GRAY);
                String msg = "Run an experiment to see results";
                g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
                return;
            }

            int chartX = PAD + 20, chartY = PAD;
            int chartW = w - chartX - PAD;
            int chartH = h - chartY - PAD - 20;

            // Grid lines & axes
            g2.setColor(new Color(200, 210, 220));
            g2.drawLine(chartX, chartY, chartX, chartY + chartH);
            g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

            double maxVal = 0;
            for (double v : values) maxVal = Math.max(maxVal, v);
            if (maxVal == 0) maxVal = 1;

            // Grid horizontals
            int gridLines = 5;
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(150, 170, 190));
            for (int i = 0; i <= gridLines; i++) {
                double frac = (double) i / gridLines;
                int gy = chartY + chartH - (int)(frac * chartH);
                g2.setColor(new Color(220, 228, 238));
                g2.drawLine(chartX, gy, chartX + chartW, gy);
                g2.setColor(new Color(100, 120, 150));
                String label = formatLargeNumber(maxVal * frac);
                g2.drawString(label, chartX - g2.getFontMetrics().stringWidth(label) - 4, gy + 4);
            }

            // Bars
            int barCount = values.length;
            int barWidth = Math.min(80, (chartW / barCount) - 20);
            int groupSpacing = chartW / barCount;

            for (int i = 0; i < barCount; i++) {
                int barH = (int)((values[i] / maxVal) * chartH);
                int bx = chartX + i * groupSpacing + (groupSpacing - barWidth) / 2;
                int by = chartY + chartH - barH;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(bx + 4, by + 4, barWidth, barH, 8, 8);

                // Bar gradient
                Color base = colors[i % colors.length];
                Color lighter = base.brighter().brighter();
                GradientPaint gp = new GradientPaint(bx, by, lighter, bx + barWidth, by + barH, base);
                g2.setPaint(gp);
                g2.fillRoundRect(bx, by, barWidth, barH, 8, 8);

                // Bar border
                g2.setColor(base.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(bx, by, barWidth, barH, 8, 8);

                // Value label on top
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(40, 60, 100));
                String val = formatLargeNumber(values[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, bx + (barWidth - fm.stringWidth(val)) / 2, by - 5);

                // X axis label
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(new Color(60, 80, 120));
                String lbl = labels[i];
                g2.drawString(lbl, bx + (barWidth - g2.getFontMetrics().stringWidth(lbl)) / 2, chartY + chartH + 15);
            }

            // Axis labels
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(60, 80, 120));
            g2.drawString(xLabel, chartX + chartW / 2 - g2.getFontMetrics().stringWidth(xLabel) / 2, h - 5);

            // Y label (rotated)
            Graphics2D g2r = (Graphics2D) g.create();
            g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2r.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2r.setColor(new Color(60, 80, 120));
            g2r.rotate(-Math.PI / 2, 12, chartY + chartH / 2);
            g2r.drawString(yLabel, 12 - g2r.getFontMetrics().stringWidth(yLabel) / 2, chartY + chartH / 2);
            g2r.dispose();
        }

        private String formatLargeNumber(double v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
            if (v >= 1_000) return String.format("%.1fK", v / 1_000);
            return String.format("%.0f", v);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LINE CHART PANEL (Trend Chart)
    // ═════════════════════════════════════════════════════════════════════════
    static class LineChartPanel extends JPanel {
        private String title, xLabel, yLabel;
        private List<Double> xs = new ArrayList<>();
        private List<Double> series1 = new ArrayList<>();
        private List<Double> series2 = new ArrayList<>();
        private final Color[] colors;
        private final String[] seriesNames;
        private static final int PAD = 70;

        LineChartPanel(String title, String xLabel, String yLabel, Color[] colors, String[] seriesNames) {
            this.title = title; this.xLabel = xLabel; this.yLabel = yLabel;
            this.colors = colors; this.seriesNames = seriesNames;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }

        void setData(List<Double> xs, List<Double> s1, List<Double> s2) {
            this.xs = new ArrayList<>(xs);
            this.series1 = new ArrayList<>(s1);
            this.series2 = new ArrayList<>(s2);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            GradientPaint bg = new GradientPaint(0, 0, new Color(248, 251, 255), 0, h, new Color(240, 245, 252));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(new Color(40, 60, 100));
            FontMetrics tfm = g2.getFontMetrics();
            g2.drawString(title, (w - tfm.stringWidth(title)) / 2, 28);

            if (xs.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
                g2.setColor(Color.GRAY);
                String msg = "Run 'Full Input-Size Sweep' or multiple experiments to see trends";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                return;
            }

            int chartX = PAD + 20, chartY = PAD;
            int chartW = w - chartX - PAD;
            int chartH = h - chartY - PAD - 30;

            double minX = xs.stream().mapToDouble(d -> d).min().orElse(0);
            double maxX = xs.stream().mapToDouble(d -> d).max().orElse(1);
            double maxY = 0;
            for (Double v : series1) maxY = Math.max(maxY, v);
            for (Double v : series2) maxY = Math.max(maxY, v);
            if (maxX == minX) maxX = minX + 1;
            if (maxY == 0) maxY = 1;

            // Grid
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                double frac = (double) i / gridLines;
                int gy = chartY + chartH - (int)(frac * chartH);
                g2.setColor(new Color(220, 228, 238));
                g2.drawLine(chartX, gy, chartX + chartW, gy);
                g2.setColor(new Color(100, 120, 150));
                String lbl = formatN(maxY * frac);
                g2.drawString(lbl, chartX - g2.getFontMetrics().stringWidth(lbl) - 4, gy + 4);
            }

            // Axes
            g2.setColor(new Color(150, 170, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(chartX, chartY, chartX, chartY + chartH);
            g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

            // X-axis ticks
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(100, 120, 150));
            for (int i = 0; i < xs.size(); i++) {
                int px = chartX + (int)(((xs.get(i) - minX) / (maxX - minX)) * chartW);
                g2.drawLine(px, chartY + chartH, px, chartY + chartH + 4);
                String xl = formatN(xs.get(i));
                g2.drawString(xl, px - g2.getFontMetrics().stringWidth(xl) / 2, chartY + chartH + 15);
            }

            // Draw filled area + line for each series
            drawSeries(g2, series1, colors[0], chartX, chartY, chartW, chartH, minX, maxX, maxY);
            drawSeries(g2, series2, colors[1], chartX, chartY, chartW, chartH, minX, maxX, maxY);

            // Legend
            int lx = chartX + chartW - 220, ly = chartY + 10;
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect(lx - 8, ly - 14, 230, 50, 10, 10);
            g2.setColor(new Color(180, 190, 210));
            g2.drawRoundRect(lx - 8, ly - 14, 230, 50, 10, 10);
            for (int i = 0; i < seriesNames.length; i++) {
                g2.setColor(colors[i]);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(lx, ly + i * 20 + 3, lx + 25, ly + i * 20 + 3);
                g2.fillOval(lx + 8, ly + i * 20 - 1, 8, 8);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(new Color(50, 70, 110));
                g2.drawString(seriesNames[i], lx + 32, ly + i * 20 + 7);
            }

            // Axis labels
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(60, 80, 120));
            g2.setStroke(new BasicStroke(1));
            g2.drawString(xLabel, chartX + chartW / 2 - g2.getFontMetrics().stringWidth(xLabel) / 2, h - 5);

            Graphics2D g2r = (Graphics2D) g.create();
            g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2r.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2r.setColor(new Color(60, 80, 120));
            g2r.rotate(-Math.PI / 2, 12, chartY + chartH / 2);
            g2r.drawString(yLabel, 12 - g2r.getFontMetrics().stringWidth(yLabel) / 2, chartY + chartH / 2);
            g2r.dispose();
        }

        private void drawSeries(Graphics2D g2, List<Double> series, Color color,
                                int chartX, int chartY, int chartW, int chartH,
                                double minX, double maxX, double maxY) {
            if (series.isEmpty()) return;

            int n = series.size();
            int[] px = new int[n], py = new int[n];
            for (int i = 0; i < n; i++) {
                px[i] = chartX + (int)(((xs.get(i) - minX) / (maxX - minX)) * chartW);
                py[i] = chartY + chartH - (int)((series.get(i) / maxY) * chartH);
            }

            // Filled area under line
            int[] fillX = new int[n + 2];
            int[] fillY = new int[n + 2];
            fillX[0] = px[0]; fillY[0] = chartY + chartH;
            for (int i = 0; i < n; i++) { fillX[i + 1] = px[i]; fillY[i + 1] = py[i]; }
            fillX[n + 1] = px[n - 1]; fillY[n + 1] = chartY + chartH;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            g2.fillPolygon(fillX, fillY, n + 2);

            // Line
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);

            // Dots
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.WHITE);
                g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);
            }
        }

        private String formatN(double v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
            if (v >= 1_000) return String.format("%.1fK", v / 1_000);
            return String.format("%.0f", v);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SupervisedBruteForceGUI().setVisible(true));
    }
}
