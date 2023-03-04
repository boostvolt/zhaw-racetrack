package ch.zhaw.pm2.racetrack.strategy;

import ch.zhaw.pm2.racetrack.Direction;

/**
 * Let the user decide the next move.
 */
public class UserMoveStrategy implements MoveStrategy {

    /**
     * {@inheritDoc}
     * Asks the user for the direction vector.
     *
     * @return next direction, null if the user terminates the game.
     */
    @Override
    public Direction nextMove() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }
}
