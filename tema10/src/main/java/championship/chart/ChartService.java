package championship.chart;

import championship.IResolver;
import championship.Player;
import championship.Position;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Строит и сохраняет графики для четырёх вариантов задания 2.
 */
public final class ChartService {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 12);

    public List<ChartInfo> createAndSaveAll(
            List<Player> players,
            IResolver resolver,
            Path outputDirectory
    ) throws IOException {
        return createAndSave(players, resolver, outputDirectory, 0);
    }

    /**
     * @param variant номер варианта от 1 до 4, либо 0 — построить все варианты
     */
    public List<ChartInfo> createAndSave(
            List<Player> players,
            IResolver resolver,
            Path outputDirectory,
            int variant
    ) throws IOException {
        if (variant < 0 || variant > 4) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 4");
        }

        Files.createDirectories(outputDirectory);

        List<Integer> variants = variant == 0
                ? List.of(1, 2, 3, 4)
                : List.of(variant);

        List<ChartInfo> result = new ArrayList<>();

        for (int currentVariant : variants) {
            JFreeChart chart = switch (currentVariant) {
                case 1 -> createPositionsPieChart(resolver);
                case 2 -> createTop10TeamsBarChart(players);
                case 3 -> createForwardsLineChart(players);
                case 4 -> createNationalitiesPieChart(players);
                default -> throw new IllegalArgumentException("Неизвестный вариант: " + currentVariant);
            };

            String fileName = switch (currentVariant) {
                case 1 -> "variant1-positions.png";
                case 2 -> "variant2-top10-teams-value.png";
                case 3 -> "variant3-forwards-goals-vs-cost.png";
                case 4 -> "variant4-nationalities.png";
                default -> throw new IllegalArgumentException("Неизвестный вариант: " + currentVariant);
            };

            Path file = outputDirectory.resolve(fileName);
            ChartUtils.saveChartAsPNG(file.toFile(), chart, WIDTH, HEIGHT);
            result.add(new ChartInfo(currentVariant, chart.getTitle().getText(), file, chart));
        }

        return result;
    }

    /**
     * Вариант 1. Доля игроков каждой позиции — круговая диаграмма.
     */
    private JFreeChart createPositionsPieChart(IResolver resolver) {
        Map<Position, Long> counts = resolver.getPlayers().stream()
                .collect(Collectors.groupingBy(
                        Player::getPosition,
                        () -> new EnumMap<>(Position.class),
                        Collectors.counting()
                ));

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Position position : Position.values()) {
            long count = counts.getOrDefault(position, 0L);
            if (count > 0) {
                dataset.setValue(position.getRussianName(), count);
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Доли позиций игроков",
                dataset,
                true,
                true,
                false
        );
        decorate(chart);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setLabelFont(TEXT_FONT);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }

    /**
     * Вариант 2. Топ-10 команд по суммарной трансферной стоимости — столбчатая диаграмма.
     */
    private JFreeChart createTop10TeamsBarChart(List<Player> players) {
        Map<String, Long> costByTeam = players.stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.summingLong(Player::getTransferCost)
                ));

        List<Map.Entry<String, Long>> top10 = costByTeam.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(10)
                .toList();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : top10) {
            dataset.addValue(entry.getValue() / 1_000_000.0, "Стоимость", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Топ-10 команд по суммарной трансферной стоимости",
                "Команда",
                "Стоимость, млн",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        decorate(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(TEXT_FONT);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 9));
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#,##0.0"))
        );
        renderer.setMaximumBarWidth(0.08);

        return chart;
    }

    /**
     * Вариант 3. Зависимость голов от трансферной стоимости для нападающих — линейный график.
     */
    private JFreeChart createForwardsLineChart(List<Player> players) {
        XYSeries series = new XYSeries("Нападающие");

        players.stream()
                .filter(player -> player.getPosition() == Position.FORWARD)
                .sorted(Comparator.comparingLong(Player::getTransferCost))
                .forEach(player -> series.add(
                        player.getTransferCost() / 1_000_000.0,
                        player.getGoals()
                ));

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Голы нападающих в зависимости от трансферной стоимости",
                "Трансферная стоимость, млн",
                "Голы",
                new XYSeriesCollection(series),
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        decorate(chart);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.getDomainAxis().setTickLabelFont(TEXT_FONT);
        plot.getRangeAxis().setTickLabelFont(TEXT_FONT);

        return chart;
    }

    /**
     * Вариант 4. Доля игроков из разных стран — круговая диаграмма.
     */
    private JFreeChart createNationalitiesPieChart(List<Player> players) {
        Map<String, Long> countByNationality = players.stream()
                .collect(Collectors.groupingBy(
                        Player::getNationality,
                        Collectors.counting()
                ));

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        countByNationality.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> dataset.setValue(entry.getKey(), entry.getValue()));

        JFreeChart chart = ChartFactory.createPieChart(
                "Доли игроков из разных стран",
                dataset,
                true,
                true,
                false
        );
        decorate(chart);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setLabelFont(TEXT_FONT);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }

    private static void decorate(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(TITLE_FONT);

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(TEXT_FONT);
            chart.getLegend().setBackgroundPaint(Color.WHITE);
        }
    }
}
