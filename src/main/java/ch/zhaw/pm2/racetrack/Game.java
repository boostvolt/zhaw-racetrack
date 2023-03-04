package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.utils.CalculationUtil.isFinishLineCrossedCorrectly;
import static ch.zhaw.pm2.racetrack.utils.CalculationUtil.isFinishLineCrossingPenalised;
import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.given.GameSpecification;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;
import ch.zhaw.pm2.racetrack.utils.CalculationUtil;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Game controller class, performing all actions to modify the game state. It contains the logic to
 * switch and move the cars, detect if they are crashed and if we have a winner.
 */
public class Game implements GameSpecification {

    /**
     * Value representing, that the game is still running, and we have no winner
     */
    public static final int NO_WINNER = -1;
    private final Track track;
    private int currentCarIndex;
    private int winnerCarIndex = NO_WINNER;

    /**
     * Instantiates a new instance of a Game.
     *
     * @param track the track on which the Game will be played.
     * @throws NullPointerException if the provided Track is null
     */
    public Game(final Track track) throws NullPointerException {
        this.track = requireNonNull(track, "Track must not be null.");
    }

    /**
     * Return the current track.
     *
     * @return the current track
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Return the number of cars.
     *
     * @return Number of cars
     */
    @Override
    public int getCarCount() {
        return track.getCarCount();
    }

    /**
     * Return the index of the current active car. Car indexes are zero-based, so the first car is
     * 0, and the last car is getCarCount() - 1.
     *
     * @return The zero-based number of the current car
     */
    @Override
    public int getCurrentCarIndex() {
        return currentCarIndex;
    }

    /**
     * Get the id of the specified car.
     *
     * @param carIndex The zero-based carIndex number
     * @return A char containing the id of the car
     */
    @Override
    public char getCarId(final int carIndex) {
        return track.getCar(carIndex).getId();
    }

    /**
     * Get the position of the specified car.
     *
     * @param carIndex The zero-based carIndex number
     * @return A PositionVector containing the car's current position
     */
    @Override
    public PositionVector getCarPosition(final int carIndex) {
        return track.getCar(carIndex).getCurrentPosition();
    }

    /**
     * Get the velocity of the specified car.
     *
     * @param carIndex The zero-based carIndex number
     * @return A PositionVector containing the car's current velocity
     */
    @Override
    public PositionVector getCarVelocity(final int carIndex) {
        return track.getCar(carIndex).getVelocity();
    }

    /**
     * Set the {@link MoveStrategy} for the specified car.
     *
     * @param carIndex        The zero-based carIndex number
     * @param carMoveStrategy The {@link MoveStrategy} to be associated with the specified car
     */
    @Override
    public void setCarMoveStrategy(final int carIndex, final MoveStrategy carMoveStrategy) {
        track.getCar(carIndex).setMoveStrategy(carMoveStrategy);
    }

    /**
     * Get the {@link MoveStrategy} of the specified car.
     *
     * @param carIndex The zero-based carIndex number
     * @return The {@link MoveStrategy} associated with the specified car
     */
    @Override
    public MoveStrategy getCarMoveStrategy(final int carIndex) {
        return track.getCar(carIndex).getMoveStrategy();
    }

    /**
     * Return the carIndex of the winner.<br/> If the game is still in progress, returns
     * {@link #NO_WINNER}.
     *
     * @return The winning car's index (zero-based, see {@link #getCurrentCarIndex()}), or
     * {@link #NO_WINNER} if the game is still in progress
     */
    @Override
    public int getWinner() {
        return winnerCarIndex;
    }

    /**
     * Execute the next turn for the current active car.
     * <p>This method changes the current car's velocity and checks on the path to the next
     * position, if it crashes (car state to crashed) or passes the finish line in the right
     * direction (set winner state).</p>
     * <p>The steps are as follows</p>
     * <ol>
     *   <li>Accelerate the current car</li>
     *   <li>Calculate the path from current (start) to next (end) position
     *       (see {@link ch.zhaw.pm2.racetrack.utils.CalculationUtil#getPassedPositions} </li>
     *   <li>Verify for each step what space type it hits:
     *      <ul>
     *          <li>TRACK: check for collision with other car (crashed &amp; don't continue), otherwise do nothing</li>
     *          <li>WALL: car did collide with the wall - crashed &amp; don't continue</li>
     *          <li>FINISH_*: car hits the finish line - wins only if it crosses the line in the correct direction</li>
     *      </ul>
     *   </li>
     *   <li>If the car crashed or wins, set its position to the crash/win coordinates</li>
     *   <li>If the car crashed, also detect if there is only one car remaining, remaining car is the winner</li>
     *   <li>Otherwise move the car to the end position</li>
     * </ol>
     * <p>The calling method must check the winner state and decide how to go on. If the winner is different
     * than {@link Game#NO_WINNER}, or the current car is already marked as crashed the method returns immediately.</p>
     *
     * @param acceleration A Direction containing the current cars acceleration vector (-1,0,1) in x
     *                     and y direction for this turn
     * @throws NullPointerException  if the provided acceleration is null
     * @throws IllegalStateException if an unspecified SpaceType is being crossed
     */
    @Override
    public void doCarTurn(final Direction acceleration)
        throws NullPointerException, IllegalStateException {
        requireNonNull(acceleration, "Acceleration must not be null.");
        final Car currentCar = track.getCar(getCurrentCarIndex());

        if (currentCar.isCrashed() || getCurrentCarIndex() == getWinner()) {
            return;
        }

        currentCar.accelerate(acceleration);
        processPositions(currentCar);
    }

    /**
     * Switches to the next car who is still in the game. Skips crashed cars.
     */
    @Override
    public void switchToNextActiveCar() {
        currentCarIndex = (getCurrentCarIndex() + 1) % getCarCount(); // switch to next car
        final Car nextCar = track.getCar(currentCarIndex);
        if (nextCar.isCrashed()) {
            switchToNextActiveCar();
        }
    }

    /**
     * Returns all the grid positions in the path between two positions, for use in determining line
     * of sight.
     *
     * @param startPosition Starting position as a PositionVector
     * @param endPosition   Ending position as a PositionVector
     * @return Intervening grid positions as a List of PositionVector's, including the starting and
     * ending positions.
     * @throws NullPointerException if the startPosition or endPosition is null
     */
    @Override
    public List<PositionVector> calculatePath(PositionVector startPosition,
        PositionVector endPosition) throws NullPointerException {
        return CalculationUtil.getPassedPositions(startPosition, endPosition);
    }

    /**
     * Processes the position vectors for the given current car and handles the resulting actions
     * based on the space types encountered.
     *
     * @param currentCar The current car being processed.
     */
    private void processPositions(final Car currentCar) {
        final List<PositionVector> passedPositions = calculatePath(currentCar.getCurrentPosition(),
            currentCar.getNextPosition());
        final Iterator<PositionVector> iterator = passedPositions.iterator();
        skipFirstPosition(iterator);
        boolean interrupted = false;

        while (iterator.hasNext() && !interrupted) {
            final PositionVector passedPosition = iterator.next();
            final SpaceType spaceTypeAtPosition = track.getSpaceTypeAtPosition(passedPosition);
            interrupted = evaluatePassedPosition(currentCar, passedPosition, spaceTypeAtPosition);
        }

        if (!interrupted) {
            currentCar.move();
        } else {
            processInterruptionImpact();
        }
    }

    /**
     * Skips the first position of the iterator. This is needed because the first position is the
     * position the car is currently on. It is not required to evaluate that position afterwards.
     *
     * @param iterator the iterator containing all passed positions
     */
    private void skipFirstPosition(Iterator<PositionVector> iterator) {
        if (iterator.hasNext()) {
            iterator.next();
        }
    }

    private boolean evaluatePassedPosition(final Car currentCar,
        final PositionVector passedPosition, final SpaceType spaceTypeAtPosition) {
        boolean interrupted = false;
        switch (spaceTypeAtPosition) {
            case WALL -> {
                currentCar.crash(passedPosition);
                interrupted = true;
            }
            case TRACK -> {
                if (track.isPositionOccupied(passedPosition)) {
                    currentCar.crash(passedPosition);
                    interrupted = true;
                }
            }
            case FINISH_UP, FINISH_DOWN, FINISH_LEFT, FINISH_RIGHT -> {
                if (track.isPositionOccupied(passedPosition)) {
                    currentCar.crash(passedPosition);
                    interrupted = true;
                } else {
                    interrupted = evaluateFinishCrossing(currentCar, spaceTypeAtPosition,
                        passedPosition);
                }
            }
            default -> throw new IllegalStateException("Crossing an unspecified SpaceType");
        }
        return interrupted;
    }

    private boolean evaluateFinishCrossing(final Car currentCar,
        final SpaceType spaceTypeAtPosition, final PositionVector passedPosition) {
        boolean crossedCorrectly = isFinishLineCrossedCorrectly(spaceTypeAtPosition,
            currentCar.getVelocity());
        boolean receivedPenalty = isFinishLineCrossingPenalised(spaceTypeAtPosition,
            currentCar.getVelocity());

        boolean won;
        if (receivedPenalty) {
            currentCar.receivePenalty();
            won = false;
        } else if (crossedCorrectly && currentCar.isPenaltyActive()) {
            currentCar.discardPenalty();
            won = false;
        } else if (crossedCorrectly && !currentCar.isPenaltyActive()) {
            winnerCarIndex = getCurrentCarIndex();
            currentCar.win(passedPosition);
            won = true;
        } else {
            won = false;
        }
        return won;
    }

    /**
     * Validates the interruption impact (crash). If there is just one car left which has not
     * crashed, that car is set as the winner.
     */
    private void processInterruptionImpact() {
        final boolean isOneCarRemaining = IntStream.range(0, track.getCarCount())
            .filter(i -> !track.getCar(i).isCrashed())
            .count() == 1;
        if (isOneCarRemaining) {
            switchToNextActiveCar();
            winnerCarIndex = getCurrentCarIndex();
        }
    }

}
