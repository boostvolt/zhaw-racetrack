package ch.zhaw.pm2.racetrack.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.zhaw.pm2.racetrack.Direction;
import org.junit.jupiter.api.Test;

/**
 * MoveStrategy Test for the {@link DoNotMoveStrategy}
 */
class DoNotMoveStrategyTest {

    /**
     * The method for this strategy should always return {@link Direction#NONE}
     */
    @Test
    void testNextMove() {
        DoNotMoveStrategy doNotMoveStrategy = new DoNotMoveStrategy();

        assertEquals(Direction.NONE, doNotMoveStrategy.nextMove());
    }

    /**
     * Testing the return value of {@link DoNotMoveStrategy#getStatistics()}.
     */
    @Test
    void testGetStatistics() {
        DoNotMoveStrategy doNotMoveStrategy = new DoNotMoveStrategy();

        assertEquals("Car waited for 0 turns.", doNotMoveStrategy.getStatistics());
        doNotMoveStrategy.nextMove();
        assertEquals("Car waited for 1 turns.", doNotMoveStrategy.getStatistics());
    }
}
