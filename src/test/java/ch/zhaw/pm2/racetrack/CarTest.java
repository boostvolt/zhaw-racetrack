package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.Car.INITIAL_VELOCITY;
import static ch.zhaw.pm2.racetrack.Direction.DOWN;
import static ch.zhaw.pm2.racetrack.Direction.DOWN_LEFT;
import static ch.zhaw.pm2.racetrack.Direction.LEFT;
import static ch.zhaw.pm2.racetrack.Direction.RIGHT;
import static ch.zhaw.pm2.racetrack.Direction.UP;
import static ch.zhaw.pm2.racetrack.Direction.UP_RIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarTest {

    private static final char CAR_ID = 'A';

    private Car car;

    @BeforeEach
    void setUp() {
        car = new Car(CAR_ID, new PositionVector(1, 2));
    }

    @Test
    void testGetId() {
        assertEquals(CAR_ID, car.getId());
    }

    @Test
    void testGetCurrentPosition() {
        assertEquals(new PositionVector(1, 2), car.getCurrentPosition());
    }

    @Test
    void testGetInitialVelocity() {
        assertEquals(INITIAL_VELOCITY, car.getVelocity());
    }

    @Test
    void testGetNextPosition() {
        accelerateAndAssertNextPosition(DOWN, 1, 3);
        accelerateAndAssertNextPosition(LEFT, 0, 3);
        accelerateAndAssertNextPosition(DOWN_LEFT, -1, 4);
        accelerateAndAssertNextPosition(RIGHT, 0, 4);
        accelerateAndAssertNextPosition(UP_RIGHT, 1, 3);
        accelerateAndAssertNextPosition(UP, 1, 2);
    }

    @Test
    void testVelocity() {
        accelerateAndAssertVelocity(DOWN, 0, 1);
        accelerateAndAssertVelocity(LEFT, -1, 1);
        accelerateAndAssertVelocity(DOWN_LEFT, -2, 2);
        accelerateAndAssertVelocity(RIGHT, -1, 2);
        accelerateAndAssertVelocity(UP_RIGHT, 0, 1);
        accelerateAndAssertVelocity(UP, 0, 0);
    }

    @Test
    void testMove() {
        moveAndAssert(DOWN, 1, 3);
        moveAndAssert(LEFT, 0, 4);
        moveAndAssert(DOWN_LEFT, -2, 6);
        moveAndAssert(RIGHT, -3, 8);
        moveAndAssert(UP_RIGHT, -3, 9);
        moveAndAssert(UP, -3, 9);
    }

    @Test
    void testCrash() {
        car.crash(new PositionVector(3, 4));
        assertTrue(car.isCrashed());
        assertEquals(new PositionVector(3, 4), car.getCurrentPosition());
    }

    @Test
    void testReceivePenalty() {
        car.receivePenalty();
        assertTrue(car.isPenaltyActive());
    }

    @Test
    void testDiscardPenalty() {
        car.receivePenalty();
        car.discardPenalty();
        assertFalse(car.isPenaltyActive());
    }

    @Test
    void testWin() {
        final PositionVector winPosition = new PositionVector(7, 7);
        car.win(winPosition);
        assertEquals(winPosition, car.getCurrentPosition());
    }

    private void accelerateAndAssertNextPosition(Direction directionToGo, int x, int y) {
        car.accelerate(directionToGo);
        assertEquals(new PositionVector(x, y), car.getNextPosition());
    }

    private void accelerateAndAssertVelocity(Direction directionToGo, int x, int y) {
        car.accelerate(directionToGo);
        assertEquals(new PositionVector(x, y), car.getVelocity());
    }

    private void moveAndAssert(final Direction directionToGo, int x, int y) {
        car.accelerate(directionToGo);
        car.move();
        assertEquals(new PositionVector(x, y), car.getCurrentPosition());
    }

}
