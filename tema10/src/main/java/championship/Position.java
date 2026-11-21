package championship;

/**
 * Позиция игрока на поле.
 */
public enum Position {
    GOALKEEPER("Вратарь"),
    DEFENDER("Защитник"),
    MIDFIELD("Полузащитник"),
    FORWARD("Нападающий");

    private final String russianName;

    Position(String russianName) {
        this.russianName = russianName;
    }

    public String getRussianName() {
        return russianName;
    }

    /**
     * Преобразует значение из CSV (например, {@code DEFENDER}) в позицию.
     */
    public static Position fromCsv(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не указана позиция игрока");
        }

        try {
            return Position.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Неизвестная позиция: " + value, exception);
        }
    }
}
