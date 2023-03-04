package ch.zhaw.pm2.racetrack;

import static java.util.Objects.requireNonNull;

import ch.zhaw.pm2.racetrack.given.CarSpecification;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;

/**
 * Class representing a car on the racetrack.<br/> Uses {@link PositionVector} to store current
 * position on the track grid and current velocity vector.<br/> Each car has an identifier character
 * which represents the car on the racetrack board.<br/> Also keeps the state, if the car is crashed
 * (not active anymore). The state can not be changed back to un crashed.<br/> The velocity is
 * changed by providing an acceleration vector.<br/> The car is able to calculate the endpoint of
 * its next position and on request moves to it.<br/>
 */
public class Car implements CarSpecification {

    static final PositionVector INITIAL_VELOCITY = new PositionVector(0, 0);

    private final char id;
    private PositionVector position;
    private PositionVector velocity;
    private boolean crashed;
    private boolean receivedPenalty;
    private MoveStrategy moveStrategy;

    /**
     * Constructor for class Car
     *
     * @param id            unique Car identification
     * @param startPosition initial position of the Car
     * @throws NullPointerException if the provided startPosition is null
     */
    public Car(char id, PositionVector startPosition) {
        this.id = id;
        position = requireNonNull(startPosition, "Start position must not be null.");
        velocity = INITIAL_VELOCITY;
    }

    /**
     * Returns Identifier of the car, which represents the car on the track
     *
     * @return identifier character
     */
    @Override
    public char getId() {
        return id;
    }

    /**
     * Returns a copy of the current position of the car on the track as a {@link PositionVector}
     *
     * @return copy of the car's current position
     */
    @Override
    public PositionVector getCurrentPosition() {
        return position;
    }

    /**
     * Returns a copy of the velocity vector of the car as a {@link PositionVector}<br/> It should
     * not be possible to change the cars velocity vector using this return value.
     *
     * @return copy of car's velocity vector
     */
    @Override
    public PositionVector getVelocity() {
        return velocity;
    }

    /**
     * Return the position that will apply after the next move at the current velocity. Does not
     * complete the move, so the current position remains unchanged.
     *
     * @return Expected position after the next move
     * @throws NullPointerException if the next position evaluates to null
     */
    @Override
    public PositionVector getNextPosition() {
        return requireNonNull(position.add(velocity), "Next position must not be null.");
    }

    /**
     * Add the specified amounts to this car's velocity.<br/> The only acceleration values allowed
     * are -1, 0 or 1 in both axis<br/> There are 9 possible acceleration vectors, which are defined
     * in {@link Direction}.<br/> Changes only velocity, not position.<br/>
     *
     * @param acceleration A Direction vector containing the amounts to add to the velocity in x and
     *                     y dimension
     * @throws NullPointerException if the provided acceleration is null
     */
    @Override
    public void accelerate(Direction acceleration) {
        velocity = velocity.add(
            requireNonNull(acceleration, "Acceleration must not be null.").getVector());
    }

    /**
     * Update this Car's position based on its current velocity.
     */
    @Override
    public void move() {
        position = getNextPosition();
    }

    /**
     * Mark this Car as being crashed at the given position.
     *
     * @param crashPosition position the car crashed.
     * @throws NullPointerException if the provided crashPosition is null
     */
    @Override
    public void crash(PositionVector crashPosition) {
        crashed = true;
        position = requireNonNull(crashPosition, "Crash position must not be null.");
    }

    /**
     * Returns whether this Car has been marked as crashed.
     *
     * @return Returns true if crash() has been called on this Car, false otherwise.
     */
    @Override
    public boolean isCrashed() {
        return crashed;
    }

    /**
     * Returns the move strategy being used by the car.
     *
     * @return The MoveStrategy instance being used by the car.
     */
    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }

    /**
     * Sets the move strategy to be used by the car.
     *
     * @param moveStrategy The MoveStrategy instance to be used by the car.
     * @throws NullPointerException if the provided MoveStrategy is null.
     */
    public void setMoveStrategy(MoveStrategy moveStrategy) throws NullPointerException {
        this.moveStrategy = requireNonNull(moveStrategy, "MoveStrategy must not be null.");
    }

    /**
     * Sets that this Car has received a penalty.
     */
    public void receivePenalty() {
        receivedPenalty = true;
    }

    /**
     * Discards any active penalty this Car has received.
     */
    public void discardPenalty() {
        receivedPenalty = false;
    }

    /**
     * Indicates if the Car has received a penalty for driving the wrong way on the track.
     *
     * @return true is the Car has received a penalty. False otherwise.
     */
    public boolean isPenaltyActive() {
        return receivedPenalty;
    }

    /**
     * Sets the position where the car has crossed the finish line.
     *
     * @param winPosition position where the car has won.
     * @throws NullPointerException if the provided winPosition is null
     */
    public void win(final PositionVector winPosition) throws NullPointerException {
        position = requireNonNull(winPosition, "winPosition must not be null");
    }

}
