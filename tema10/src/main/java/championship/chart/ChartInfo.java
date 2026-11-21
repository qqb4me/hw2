package championship.chart;

import org.jfree.chart.JFreeChart;

import java.nio.file.Path;

/**
 * Готовый график вместе с его номером варианта и файлом, в который он сохранён.
 */
public record ChartInfo(int variant, String title, Path file, JFreeChart chart) {
}
