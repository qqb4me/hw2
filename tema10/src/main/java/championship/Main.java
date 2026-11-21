package championship;

import championship.chart.ChartInfo;
import championship.chart.ChartService;
import championship.chart.ChartViewer;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Main {
    private static final Path DEFAULT_CSV = Path.of("data", "fakePlayers.csv");
    private static final Path DEFAULT_CHARTS_DIR = Path.of("charts");

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);

        List<Player> players = new CsvPlayersReader(options.csv()).read();
        PlayerResolver resolver = new PlayerResolver(players);

        printReport(resolver);

        ChartService chartService = new ChartService();
        List<ChartInfo> charts = chartService.createAndSave(
                players,
                resolver,
                options.chartsDirectory(),
                options.variant()
        );

        System.out.println();
        System.out.println("Графики сохранены:");
        for (ChartInfo chart : charts) {
            System.out.printf("  Вариант %d — %s%n", chart.variant(), chart.file().toAbsolutePath());
        }

        if (!options.noGui() && !GraphicsEnvironment.isHeadless()) {
            ChartViewer.show(charts);
        }
    }

    private static void printReport(IResolver resolver) {
        System.out.println("=== Чемпионат: результаты (Stream API) ===");
        System.out.println("Загружено игроков: " + resolver.getPlayers().size());
        System.out.println();

        System.out.println("1. Игроков без агентства: " + resolver.getCountWithoutAgency());
        System.out.println("2. Максимальное число голов защитника: " + resolver.getMaxDefenderGoalsCount());
        System.out.println("3. Позиция самого дорогого немецкого игрока: "
                + resolver.getTheExpensiveGermanPlayerPosition());

        System.out.println("4. Игроки по позициям:");
        for (Map.Entry<String, String> entry : resolver.getPlayersByPosition().entrySet()) {
            System.out.println("   " + entry.getKey() + ": " + entry.getValue());
        }

        Set<String> teams = resolver.getTeams();
        System.out.println("5. Команды чемпионата (" + teams.size() + "): " + String.join(", ", teams));

        System.out.println("6. Топ-5 команд по забитым мячам:");
        for (Map.Entry<String, Integer> entry : resolver.getTop5TeamsByGoalsCount().entrySet()) {
            System.out.println("   " + entry.getKey() + " — " + entry.getValue());
        }

        System.out.println("7. Агентство с минимальной суммарной стоимостью игроков: "
                + resolver.getAgencyWithMinPlayersCount());
        System.out.println("8. Самая грубая команда (среднее число удалений): "
                + resolver.getTheRudestTeam());
    }

    private record Options(Path csv, Path chartsDirectory, int variant, boolean noGui) {

        private static Options parse(String[] args) {
            Path csv = null;
            Path chartsDirectory = DEFAULT_CHARTS_DIR;
            int variant = 0;
            boolean noGui = false;

            for (String argument : args) {
                if (argument.equals("--no-gui")) {
                    noGui = true;
                } else if (argument.startsWith("--variant=")) {
                    variant = parseVariant(argument.substring("--variant=".length()));
                } else if (argument.startsWith("--number=")) {
                    int studentNumber = parsePositiveInt(argument.substring("--number=".length()), "номер в списке");
                    variant = studentNumber % 4 + 1;
                } else if (argument.startsWith("--charts-dir=")) {
                    chartsDirectory = Path.of(argument.substring("--charts-dir=".length()));
                } else if (argument.startsWith("--")) {
                    throw new IllegalArgumentException("Неизвестный параметр: " + argument);
                } else {
                    if (csv != null) {
                        throw new IllegalArgumentException("Можно указать только один CSV-файл");
                    }
                    csv = Path.of(argument);
                }
            }

            if (csv == null) {
                csv = findDefaultCsv();
            }

            return new Options(csv, chartsDirectory, variant, noGui);
        }

        private static int parseVariant(String value) {
            int variant = parsePositiveInt(value, "номер варианта");
            if (variant < 1 || variant > 4) {
                throw new IllegalArgumentException("Номер варианта должен быть от 1 до 4");
            }
            return variant;
        }

        private static int parsePositiveInt(String value, String name) {
            try {
                int number = Integer.parseInt(value);
                if (number < 0) {
                    throw new IllegalArgumentException(name + " не может быть отрицательным");
                }
                return number;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Некорректный " + name + ": " + value, exception);
            }
        }

        private static Path findDefaultCsv() {
            List<Path> candidates = List.of(
                    DEFAULT_CSV,
                    Path.of("tema10", "data", "fakePlayers.csv")
            );

            for (Path candidate : candidates) {
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }

            throw new IllegalArgumentException(
                    "Не найден fakePlayers.csv. Укажите путь к файлу первым аргументом."
            );
        }
    }
}
