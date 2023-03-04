package ch.zhaw.pm2.racetrack;

import ch.zhaw.pm2.racetrack.given.GameSpecification;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;

import java.util.List;

/**
 * Game controller class, performing all actions to modify the game state.
 * It contains the logic to switch and move the cars, detect if they are crashed
 * and if we have a winner.
 */
public class Game implements GameSpecification {
    /** Value representing, that the game is still running, and we have no winner */
    public static final int NO_WINNER = -1;

    /**
     * Return the number of cars.
     * @return Number of cars
     */
    @Override
    public int getCarCount() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Return the index of the current active car.
     * Car indexes are zero-based, so the first car is 0, and the last car is getCarCount() - 1.
     * @return The zero-based number of the current car
     */
    @Override
    public int getCurrentCarIndex() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Get the id of the specified car.
     * @param carIndex The zero-based carIndex number
     * @return A char containing the id of the car
     */
    @Override
    public char getCarId(int carIndex) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Get the position of the specified car.
     * @param carIndex The zero-based carIndex number
     * @return A PositionVector containing the car's current position
     */
    @Override
    public PositionVector getCarPosition(int carIndex) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Get the velocity of the specified car.
     * @param carIndex The zero-based carIndex number
     * @return A PositionVector containing the car's current velocity
     */
    @Override
    public PositionVector getCarVelocity(int carIndex) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Set the {@link MoveStrategy} for the specified car.
     * @param carIndex The zero-based carIndex number
     * @param carMoveStrategy The {@link MoveStrategy} to be associated with the specified car
     */
    @Override
    public void setCarMoveStrategy(int carIndex, MoveStrategy carMoveStrategy) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Get the {@link MoveStrategy} of the specified car.
     * @param carIndex The zero-based carIndex number
     * @return The {@link MoveStrategy} associated with the specified car
     */
    @Override
    public MoveStrategy getCarMoveStrategy(int carIndex) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Return the carIndex of the winner.<br/>
     * If the game is still in progress, returns {@link #NO_WINNER}.
     * @return The winning car's index (zero-based, see {@link #getCurrentCarIndex()}),
     * or {@link #NO_WINNER} if the game is still in progress
     */
    @Override
    public int getWinner() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Execute the next turn for the current active car.
     * <p>This method changes the current car's velocity and checks on the path to the next position,
     * if it crashes (car state to crashed) or passes the finish line in the right direction (set winner state).</p>
     * <p>The steps are as follows</p>
     * <ol>
     *   <li>Accelerate the current car</li>
     *   <li>Calculate the path from current (start) to next (end) position
     *       (see {@link Game#calculatePath(PositionVector, PositionVector)})</li>
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
     * @param acceleration A Direction containing the current cars acceleration vector (-1,0,1) in x and y direction
     *                     for this turn
     */
    @Override
    public void doCarTurn(Direction acceleration) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Switches to the next car who is still in the game. Skips crashed cars.
     */
    @Override
    public void switchToNextActiveCar() {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

    /**
     * Returns all the grid positions in the path between two positions, for use in determining line of sight. <br>
     * Determine the 'pixels/positions' on a raster/grid using Bresenham's line algorithm.
     * (https://de.wikipedia.org/wiki/Bresenham-Algorithmus)<br>
     * Basic steps are <ul>
     *   <li>Detect which axis of the distance vector is longer (faster movement)</li>
     *   <li>for each pixel on the 'faster' axis calculate the position on the 'slower' axis.</li>
     * </ul>
     * Direction of the movement has to correctly considered.
     *
     * @param startPosition Starting position as a PositionVector
     * @param endPosition Ending position as a PositionVector
     * @return Intervening grid positions as a List of PositionVector's, including the starting and ending positions.
     */
    @Override
    public List<PositionVector> calculatePath(PositionVector startPosition, PositionVector endPosition) {
        // TODO: implementation
        throw new UnsupportedOperationException();
    }

}
