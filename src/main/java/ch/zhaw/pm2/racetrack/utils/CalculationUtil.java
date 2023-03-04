package ch.zhaw.pm2.racetrack.utils;

import static java.lang.Integer.signum;
import static java.lang.Math.abs;
import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.PositionVector;
import ch.zhaw.pm2.racetrack.SpaceType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A utility class containing static methods for performing calculations related to the track.
 */
public final class CalculationUtil {

    private static final Map<SpaceType, Function<PositionVector, Boolean>> CROSSING_EVALUATIONS = new EnumMap<>(
        SpaceType.class);
    private static final Map<SpaceType, Function<PositionVector, Boolean>> PENALTY_EVALUATIONS = new EnumMap<>(
        SpaceType.class);

    static {
        CROSSING_EVALUATIONS.put(SpaceType.FINISH_UP, velocity -> velocity.getY() < 0);
        CROSSING_EVALUATIONS.put(SpaceType.FINISH_DOWN, velocity -> velocity.getY() > 0);
        CROSSING_EVALUATIONS.put(SpaceType.FINISH_LEFT, velocity -> velocity.getX() < 0);
        CROSSING_EVALUATIONS.put(SpaceType.FINISH_RIGHT, velocity -> velocity.getX() > 0);

        PENALTY_EVALUATIONS.put(SpaceType.FINISH_UP, velocity -> velocity.getY() > 0);
        PENALTY_EVALUATIONS.put(SpaceType.FINISH_DOWN, velocity -> velocity.getY() < 0);
        PENALTY_EVALUATIONS.put(SpaceType.FINISH_LEFT, velocity -> velocity.getX() > 0);
        PENALTY_EVALUATIONS.put(SpaceType.FINISH_RIGHT, velocity -> velocity.getX() < 0);
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CalculationUtil() {
    }

    /**
     * Returns all the grid positions in the path between two positions, for use in determining line
     * of sight. <br> Determine the 'pixels/positions' on a raster/grid using Bresenham's line
     * algorithm. (<a href="https://de.wikipedia.org/wiki/Bresenham-Algorithmus">Wikipedia</a>)<br>
     * Basic steps are <ul>
     * <li>Detect which axis of the distance vector is longer (faster movement)</li>
     * <li>for each pixel on the 'faster' axis calculate the position on the 'slower' axis.</li>
     * </ul>
     * Direction of the movement has to correctly considered.
     *
     * @param startPosition Starting position as a PositionVector
     * @param endPosition   Ending position as a PositionVector
     * @return Intervening grid positions as a List of PositionVector's, including the starting and
     * ending positions.
     * @throws NullPointerException is the startPosition or the endPosition is null
     */
    public static List<PositionVector> getPassedPositions(final PositionVector startPosition,
        final PositionVector endPosition) throws NullPointerException {
        requireNonNull(startPosition, "startPosition must not be null");
        requireNonNull(endPosition, "endPosition must not be null");
        final List<PositionVector> pointList = new ArrayList<>();
        // Use Bresenham's algorithm to determine positions.
        // Relative Distance (x & y-axis) between end- and starting position
        int diffX = endPosition.getX() - startPosition.getX();
        int diffY = endPosition.getY() - startPosition.getY();

        // Absolute distance (x & y-axis) between end- and starting position
        int distX = abs(diffX);
        int distY = abs(diffY);

        // Direction of vector on x & y axis (-1: to left/down, 0: none, +1 : to right/up)
        int dirX = signum(diffX);
        int dirY = signum(diffY);

        // Determine which axis is the fast direction and set parallel/diagonal step values
        int parallelStepX;
        int parallelStepY;
        int diagonalStepX;
        int diagonalStepY;
        int distanceSlowAxis;
        int distanceFastAxis;
        if (distX > distY) {
            // x-axis is the 'fast' direction
            parallelStepX = dirX;
            parallelStepY = 0; // parallel step only moves in x direction
            diagonalStepX = dirX;
            diagonalStepY = dirY; // diagonal step moves in both directions
            distanceSlowAxis = distY;
            distanceFastAxis = distX;
        } else {
            // y-axis is the 'fast' direction
            parallelStepX = 0;
            parallelStepY = dirY; // parallel step only moves in y direction
            diagonalStepX = dirX;
            diagonalStepY = dirY; // diagonal step moves in both directions
            distanceSlowAxis = distX;
            distanceFastAxis = distY;
        }
        // initialize path loop
        int x = startPosition.getX();
        int y = startPosition.getY();
        int error = distanceFastAxis / 2; // set to half distance to get a good starting value
        // Add starting position to the list.
        pointList.add(new PositionVector(x, y));

        // path loop:
        // by default step parallel to the fast axis.
        // if error value gets negative take a diagonal step
        // this happens approximately every (distanceFastAxis / distanceSlowAxis) steps
        for (int step = 0; step < distanceFastAxis; step++) {
            error -= distanceSlowAxis; // update error value
            if (error < 0) {
                error += distanceFastAxis; // correct error value to be positive again
                // step into slow direction; diagonalStepX
                x += diagonalStepX;
                y += diagonalStepY;
            } else {
                // step into fast direction; parallel step
                x += parallelStepX;
                y += parallelStepY;
            }
            // Add position to the list.
            pointList.add(new PositionVector(x, y));
        }
        return pointList;
    }

    /**
     * Returns whether the finish line has been crossed in the correct direction. If the provided
     * spaceType is not a finish-type, false will be returned.
     *
     * @param spaceType       the space type which has been crossed
     * @param currentVelocity the velocity to check against.
     * @return true if the finish line has been crossed correctly. False otherwise.
     */
    public static boolean isFinishLineCrossedCorrectly(final SpaceType spaceType,
        final PositionVector currentVelocity) {
        return CROSSING_EVALUATIONS.getOrDefault(spaceType, velocity -> false)
            .apply(currentVelocity);
    }

    /**
     * Returns whether the finish line has been crossed incorrectly so that a penalty was caused. If
     * the provided * spaceType is not a finish-type, false will be returned.
     *
     * @param spaceType       the space type which has been crossed
     * @param currentVelocity the velocity to check against.
     * @return true if the finish line has been crossed incorrectly. False otherwise.
     */
    public static boolean isFinishLineCrossingPenalised(final SpaceType spaceType,
        final PositionVector currentVelocity) {
        return PENALTY_EVALUATIONS.getOrDefault(spaceType, velocity -> false)
            .apply(currentVelocity);
    }

}
