package ch.zhaw.pm2.racetrack.strategy;

import static java.lang.String.format;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;

/**
 * MovementStrategy: Do not accelerate in any direction. This Class only uses the default
 * constructor since there is no variability in its function.
 */
public class DoNotMoveStrategy implements MoveStrategy {

    private Integer counter = 0;

    /**
     * {@inheritDoc}
     *
     * @return always {@link Direction#NONE}
     */
    @Override
    public Direction nextMove() {
        counter++;
        return Direction.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTurnMessage(final Car car) {
        return format("Car %s is not moving.", car.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatistics() {
        return format("Car waited for %s turns.", counter);
    }
}
