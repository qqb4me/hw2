package championship;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Реализация всех запросов из задания через Stream API.
 */
public final class PlayerResolver implements IResolver {
    private static final Predicate<Player> WITHOUT_AGENCY = player -> !player.hasAgency();
    private static final Predicate<Player> IS_GERMAN =
            player -> "Germany".equalsIgnoreCase(player.getNationality());
    private static final Predicate<Player> IS_DEFENDER =
            player -> player.getPosition() == Position.DEFENDER;
    private static final ToIntFunction<Player> GOALS = Player::getGoals;

    private final List<Player> players;

    public PlayerResolver(List<Player> players) {
        this.players = List.copyOf(players);
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public int getCountWithoutAgency() {
        return (int) players.stream()
                .filter(WITHOUT_AGENCY)
                .count();
    }

    @Override
    public int getMaxDefenderGoalsCount() {
        return players.stream()
                .filter(IS_DEFENDER)
                .mapToInt(GOALS)
                .max()
                .orElse(0);
    }

    @Override
    public String getTheExpensiveGermanPlayerPosition() {
        return players.stream()
                .filter(IS_GERMAN)
                .max(Comparator.comparingLong(Player::getTransferCost))
                .map(player -> player.getPosition().getRussianName())
                .orElse("");
    }

    @Override
    public Map<String, String> getPlayersByPosition() {
        return players.stream()
                .collect(Collectors.groupingBy(
                        player -> player.getPosition().getRussianName(),
                        LinkedHashMap::new,
                        Collectors.mapping(Player::getName, Collectors.joining(", "))
                ));
    }

    @Override
    public Set<String> getTeams() {
        return players.stream()
                .map(Player::getTeam)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Map<String, Integer> getTop5TeamsByGoalsCount() {
        Map<String, Integer> goalsByTeam = players.stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.summingInt(Player::getGoals)
                ));

        Comparator<Map.Entry<String, Integer>> byGoalsDescending =
                Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey());

        return goalsByTeam.entrySet().stream()
                .sorted(byGoalsDescending)
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    @Override
    public String getAgencyWithMinPlayersCount() {
        Map<String, Long> totalCostByAgency = players.stream()
                .filter(Player::hasAgency)
                .collect(Collectors.groupingBy(
                        Player::getAgency,
                        Collectors.summingLong(Player::getTransferCost)
                ));

        Comparator<Map.Entry<String, Long>> byCostAscending =
                Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey());

        return totalCostByAgency.entrySet().stream()
                .min(byCostAscending)
                .map(Map.Entry::getKey)
                .orElse("");
    }

    @Override
    public String getTheRudestTeam() {
        Map<String, Double> averageRedCardsByTeam = players.stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.averagingInt(Player::getRedCards)
                ));

        Comparator<Map.Entry<String, Double>> byAverageDescending =
                Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey());

        return averageRedCardsByTeam.entrySet().stream()
                .min(byAverageDescending)
                .map(Map.Entry::getKey)
                .orElse("");
    }
}
