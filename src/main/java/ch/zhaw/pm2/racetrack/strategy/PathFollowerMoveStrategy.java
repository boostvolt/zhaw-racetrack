package ch.zhaw.pm2.racetrack.strategy;

import ch.zhaw.pm2.racetrack.Direction;

/**
 * Determines the next move based on a file containing points on a path.
 */
public class PathFollowerMoveStrategy implements MoveStrategy {

    /**
     * {@inheritDoc}
     *
     * @return next direction to follow the given path, {@link Direction#NONE} if there are no more coordinates available
     */
    @Override
    public Direction nextMove() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }
}
