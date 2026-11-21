package championship;

import java.util.Objects;

/**
 * Данные об участии футболиста в чемпионате.
 */
public final class Player {
    private final String name;
    private final String team;
    private final String city;
    private final Position position;
    private final String nationality;
    private final String agency;
    private final long transferCost;
    private final int participations;
    private final int goals;
    private final int assists;
    private final int yellowCards;
    private final int redCards;

    public Player(
            String name,
            String team,
            String city,
            Position position,
            String nationality,
            String agency,
            long transferCost,
            int participations,
            int goals,
            int assists,
            int yellowCards,
            int redCards
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.team = Objects.requireNonNull(team, "team");
        this.city = Objects.requireNonNull(city, "city");
        this.position = Objects.requireNonNull(position, "position");
        this.nationality = Objects.requireNonNull(nationality, "nationality");
        this.agency = agency == null ? "" : agency;
        this.transferCost = transferCost;
        this.participations = participations;
        this.goals = goals;
        this.assists = assists;
        this.yellowCards = yellowCards;
        this.redCards = redCards;
    }

    public String getName() {
        return name;
    }

    public String getTeam() {
        return team;
    }

    public String getCity() {
        return city;
    }

    public Position getPosition() {
        return position;
    }

    public String getNationality() {
        return nationality;
    }

    public String getAgency() {
        return agency;
    }

    public boolean hasAgency() {
        return !agency.isBlank();
    }

    public long getTransferCost() {
        return transferCost;
    }

    public int getParticipations() {
        return participations;
    }

    public int getGoals() {
        return goals;
    }

    public int getAssists() {
        return assists;
    }

    public int getYellowCards() {
        return yellowCards;
    }

    public int getRedCards() {
        return redCards;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", team='" + team + '\'' +
                ", position=" + position +
                ", nationality='" + nationality + '\'' +
                ", agency='" + agency + '\'' +
                ", transferCost=" + transferCost +
                ", goals=" + goals +
                '}';
    }
}
