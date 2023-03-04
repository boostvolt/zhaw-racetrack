package ch.zhaw.pm2.racetrack;

import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing a direction on the track grid. Also representing the possible acceleration
 * values.
 */
public enum Direction {
    DOWN_LEFT(new PositionVector(-1, 1)),
    DOWN(new PositionVector(0, 1)),
    DOWN_RIGHT(new PositionVector(1, 1)),
    LEFT(new PositionVector(-1, 0)),
    NONE(new PositionVector(0, 0)),
    RIGHT(new PositionVector(1, 0)),
    UP_LEFT(new PositionVector(-1, -1)),
    UP(new PositionVector(0, -1)),
    UP_RIGHT(new PositionVector(1, -1));

    private final PositionVector vector;

    Direction(final PositionVector vector) {
        this.vector = vector;
    }

    /**
     * Check if given {@link String} is a {@link Direction}
     *
     * @param rawDirection {@link Direction} as {@link String}
     * @return {@code true} if {@link String} is a {@link Direction}
     */
    public static boolean isValidDirection(final String rawDirection) {
        return EnumSet.allOf(Direction.class).stream()
            .anyMatch(direction -> direction.name().equals(rawDirection));
    }

    /**
     * Returns the PositionVector object associated with this position.
     *
     * @return the PositionVector object associated with this position
     */
    public PositionVector getVector() {
        return vector;
    }

    /**
     * Returns a Set of all Directions which cause a change in velocity in x and/or y-axis.
     *
     * @return the list of moving directions.
     */
    public static Set<Direction> getMovingDirections() {
        return EnumSet.allOf(Direction.class).stream()
            .filter(direction -> direction != NONE)
            .collect(toSet());
    }

}
