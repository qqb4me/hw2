package championship.chart;

import org.jfree.chart.ChartPanel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.List;

/**
 * Показывает построенные графики в окне с вкладками.
 */
public final class ChartViewer {
    private ChartViewer() {
    }

    public static void show(List<ChartInfo> charts) {
        if (charts.isEmpty()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Чемпионат — визуализация");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane tabs = new JTabbedPane();
            for (ChartInfo chartInfo : charts) {
                ChartPanel panel = new ChartPanel(chartInfo.chart());
                panel.setPreferredSize(new Dimension(1000, 700));
                tabs.addTab("Вариант " + chartInfo.variant(), panel);
            }

            frame.setContentPane(tabs);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
