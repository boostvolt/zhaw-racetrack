package ch.zhaw.pm2.racetrack.strategy;

import ch.zhaw.pm2.racetrack.Direction;

/**
 * Do not accelerate in any direction.
 */
public class DoNotMoveStrategy implements MoveStrategy {
    /**
     * {@inheritDoc}
     *
     * @return always {@link Direction#NONE}
     */
    @Override
    public Direction nextMove() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }
}
