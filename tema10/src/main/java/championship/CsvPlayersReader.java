package championship;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Читает игроков из CSV-файла с разделителем {@code ;}.
 */
public final class CsvPlayersReader {
    private static final char DELIMITER = ';';
    private static final int COLUMN_COUNT = 12;

    private final Path file;

    public CsvPlayersReader(Path file) {
        this.file = file;
    }

    public List<Player> read() throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Файл с данными не найден: " + file.toAbsolutePath());
        }

        List<Player> players = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    line = removeBom(line);
                }

                if (line.isBlank()) {
                    continue;
                }

                List<String> columns = parseLine(line, lineNumber);

                if (lineNumber == 1 && columns.get(0).equalsIgnoreCase("Name")) {
                    continue;
                }

                if (columns.size() != COLUMN_COUNT) {
                    throw new IOException(
                            "Строка " + lineNumber + ": ожидалось " + COLUMN_COUNT
                                    + " колонок, найдено " + columns.size()
                    );
                }

                players.add(toPlayer(columns, lineNumber));
            }
        }

        return players;
    }

    private static Player toPlayer(List<String> columns, int lineNumber) throws IOException {
        try {
            return new Player(
                    columns.get(0).trim(),
                    columns.get(1).trim(),
                    columns.get(2).trim(),
                    Position.fromCsv(columns.get(3)),
                    columns.get(4).trim(),
                    columns.get(5).trim(),
                    parseLong(columns.get(6)),
                    parseInt(columns.get(7)),
                    parseInt(columns.get(8)),
                    parseInt(columns.get(9)),
                    parseInt(columns.get(10)),
                    parseInt(columns.get(11))
            );
        } catch (RuntimeException exception) {
            throw new IOException("Строка " + lineNumber + ": " + exception.getMessage(), exception);
        }
    }

    private static int parseInt(String value) {
        return Integer.parseInt(value.trim());
    }

    private static long parseLong(String value) {
        return Long.parseLong(value.trim());
    }

    private static String removeBom(String line) {
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }

    /**
     * Разбирает строку CSV. Поддерживаются значения в кавычках и экранированные кавычки.
     */
    private static List<String> parseLine(String line, int lineNumber) throws IOException {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char symbol = line.charAt(index);

            if (symbol == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (symbol == DELIMITER && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(symbol);
            }
        }

        if (quoted) {
            throw new IOException("Строка " + lineNumber + ": незакрытая кавычка");
        }

        columns.add(current.toString());
        return columns;
    }
}
