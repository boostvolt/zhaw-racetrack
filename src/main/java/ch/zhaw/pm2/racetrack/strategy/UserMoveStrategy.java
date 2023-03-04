package ch.zhaw.pm2.racetrack.strategy;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.ui.ConsoleUserInterface;
import ch.zhaw.pm2.racetrack.ui.UserInterface;

/**
 * {@link MoveStrategy} to let the user decide the next move. Extends {@link ConsoleUserInterface}
 * to allow more interactivity for the user.
 */
public class UserMoveStrategy implements MoveStrategy {

    private final UserInterface userInterface;
    private final char carId;

    private Integer counter = 0;

    /**
     * @param userInterface {@link UserInterface} to inform and prompt user.
     * @param carId         id of the {@link Car}.
     * @throws NullPointerException if any variable is null.
     */
    public UserMoveStrategy(UserInterface userInterface, char carId) throws NullPointerException {
        this.userInterface = requireNonNull(userInterface);
        this.carId = carId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction nextMove() {
        counter++;
        return userInterface.retrieveDirection(carId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTurnMessage(final Car car) {
        final StringBuilder sb = new StringBuilder();

        if (car.getVelocity().getX() > NOT_MOVING) {
            sb.append(format("Right: %s ", car.getVelocity().getX()));
        } else if (car.getVelocity().getX() < NOT_MOVING) {
            sb.append(format("Left: %s ", Math.abs(car.getVelocity().getX())));
        }

        if (car.getVelocity().getY() > NOT_MOVING) {
            sb.append(format("Down: %s", car.getVelocity().getY()));
        } else if (car.getVelocity().getY() < NOT_MOVING) {
            sb.append(format("Up: %s", Math.abs(car.getVelocity().getY())));
        }

        return format("The current velocity for Car %s is %s", car.getId(),
            sb.isEmpty() ? "Not moving" : sb.toString().trim());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatistics() {
        return format(CAR_WON_STATS_TEXT, counter);
    }
}
