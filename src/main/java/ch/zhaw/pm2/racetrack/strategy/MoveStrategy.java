package ch.zhaw.pm2.racetrack.strategy;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;

/**
 * Move Strategy, the different cars may choose to determine the next direction to accelerate.
 */
public interface MoveStrategy {

    int NOT_MOVING = 0;
    String CAR_WON_STATS_TEXT = "Car won after %s turns.";

    /**
     * Determine direction to accelerate in the next move.
     *
     * @return Direction vector to accelerate in the next move. null will terminate the game.
     */
    Direction nextMove();

    /**
     * Returns information for the players, about the cars current movement.
     *
     * @param car {@link Car} object that the Message is printed out for.
     * @return Message of what the car does during its turn.
     */
    String getTurnMessage(final Car car);

    /**
     * Returns statistical information about the current state of the {@link Car}s movement.
     *
     * @return Message as {@link String} containing the statistical information.
     */
    String getStatistics();

    /**
     * Possible Move Strategies which can be selected. This shall not be altered!
     */
    enum StrategyType {
        DO_NOT_MOVE, USER, MOVE_LIST, PATH_FOLLOWER, PATH_FINDER
    }
}
