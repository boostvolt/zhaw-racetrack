package ch.zhaw.pm2.racetrack;

import static ch.zhaw.pm2.racetrack.Direction.LEFT;
import static ch.zhaw.pm2.racetrack.Direction.NONE;
import static ch.zhaw.pm2.racetrack.Direction.RIGHT;
import static ch.zhaw.pm2.racetrack.Direction.UP;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.zhaw.pm2.racetrack.strategy.DoNotMoveStrategy;
import ch.zhaw.pm2.racetrack.strategy.MoveStrategy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Game} class
 */
class GameTest {

    private static final int CAR_INDEX_ZERO = 0;
    private static final int CAR_INDEX_ONE = 1;
    private static final char CAR_ID_ZERO = 'a';
    private static final char CAR_ID_ONE = 'b';
    private static final PositionVector START_POS = new PositionVector(24, 22);

    private Game game;
    private Track track;
    private final MoveStrategy moveStrategy = new DoNotMoveStrategy();

    @BeforeEach
    void setUp() throws InvalidFileFormatException, IOException {
        track = setTestTrack("validTrack");
        game = new Game(track);
    }

    /**
     * Tests if the getTrack method returns the same track Object as it received.
     */
    @Test
    void testGetTrack() {
        assertEquals(game.getTrack(), track);
    }

    /**
     * Tests if the getCarCount method returns the correct number of cars.
     */
    @Test
    void testGetCarCount() {
        assertEquals(2, game.getCarCount());
    }

    /**
     * Tests if the method getCurrentCarIndex returns the correct carIndex. Also tests if the method
     * returns the correct index after the game switched to the next player.
     */
    @Test
    void testGetCurrentCarIndex() {
        assertEquals(CAR_INDEX_ZERO, game.getCurrentCarIndex());
        game.switchToNextActiveCar();
        assertEquals(CAR_INDEX_ONE, game.getCurrentCarIndex());
    }

    /**
     * Tests if the getCarId method returns the correct char for each car.
     */
    @Test
    void testGetCarId() {
        assertEquals(CAR_ID_ZERO, game.getCarId(CAR_INDEX_ZERO));
        assertEquals(CAR_ID_ONE, game.getCarId(CAR_INDEX_ONE));
    }

    /**
     * Tests if the getCarPosition returns the correct starting position.
     */
    @Test
    void testGetCarPosition() {
        assertEquals(START_POS, game.getCarPosition(CAR_INDEX_ZERO));
    }

    /**
     * Tests if the method getCarVelocity gets the correct velocity.
     */
    @Test
    void testGetCarVelocity() {
        assertEquals(game.getCarVelocity(CAR_INDEX_ZERO), new PositionVector(0, 0));
    }

    /**
     * Tests if the setCarMoveStrategy and the getCarMoveStrategy work properly.
     */
    @Test
    void testSettingAndRetrievingCarMoveStrategy() {
        game.setCarMoveStrategy(CAR_INDEX_ZERO, moveStrategy);
        assertEquals(game.getCarMoveStrategy(CAR_INDEX_ZERO), moveStrategy);
    }

    /**
     * Tests if the method getWinner returns the correct winner.
     *
     * @throws InvalidFileFormatException {@link InvalidFileFormatException} in some circumstance.
     * @throws IOException                {@link java.io.IOException} in some circumstance.
     */
    @Test
    void testGetWinner() throws InvalidFileFormatException, IOException {
        assertEquals(Game.NO_WINNER, game.getWinner());

        game = new Game(setTestTrack("gameTestTrack"));
        simulateWin(game);
        assertEquals(CAR_INDEX_ZERO, game.getWinner());
    }

    /**
     * Tests if the method doCarTurn works properly. It first tests, if the car moves correctly.
     * Secondly it tests if the car doesn't move if its crashed, or it has won the game.
     */
    @Test
    void testDoCarTurn() throws InvalidFileFormatException, IOException {
        game.doCarTurn(RIGHT);
        assertEquals(new PositionVector(25, 22), game.getCarPosition(CAR_INDEX_ZERO));

        game.doCarTurn(UP);
        game.doCarTurn(RIGHT);
        assertEquals(new PositionVector(26, 21), game.getCarPosition(CAR_INDEX_ZERO));

        game = new Game(setTestTrack("gameTestTrack"));
        simulateWin(game);
        game.doCarTurn(LEFT);
        assertEquals(new PositionVector(10, 3), game.getCarPosition(CAR_INDEX_ZERO));
    }

    /**
     * Tests if the method switchToNextActiveCar works properly.
     */
    @Test
    void testSwitchToNextActiveCar() throws InvalidFileFormatException, IOException {
        assertEquals(CAR_INDEX_ZERO, game.getCurrentCarIndex());
        game.switchToNextActiveCar();
        assertEquals(CAR_INDEX_ONE, game.getCurrentCarIndex());

        game = new Game(setTestTrack("gameTestTrackThreeCars"));
        game.doCarTurn(UP);

        for (int i = 0; i < 3; i++) {
            game.switchToNextActiveCar();
        }

        assertEquals(CAR_INDEX_ONE, game.getCurrentCarIndex());
    }

    /**
     * Tests if the CalculatePath method works as intended. For extensive testing see
     * {@link ch.zhaw.pm2.racetrack.utils.CalculationUtil}.
     */
    @Test
    void testCalculatePath() {
        PositionVector startPosition = new PositionVector(0, 0);
        PositionVector endPosition = new PositionVector(3, 3);

        final List<PositionVector> pointList = new ArrayList<>();
        pointList.add(startPosition);
        pointList.add(new PositionVector(1, 1));
        pointList.add(new PositionVector(2, 2));
        pointList.add(endPosition);
        assertEquals(pointList, game.calculatePath(startPosition, endPosition));
    }

    /**
     * Tests that the penalty is activated when the finish line is crossed in the wrong direction.
     */
    @Test
    void testPenaltyActive() {
        simulatePenalty();
        assertTrue(game.getTrack().getCar(CAR_INDEX_ZERO).isPenaltyActive());
        assertFalse(game.getTrack().getCar(CAR_INDEX_ONE).isPenaltyActive());
    }

    /**
     * Tests that an active penalty is discarded after crossing the finish line correctly.
     */
    @Test
    void testPenaltyDiscard() {
        simulatePenalty();
        resetPenalty();
        assertFalse(game.getTrack().getCar(CAR_INDEX_ZERO).isPenaltyActive());
    }

    private Track setTestTrack(String fileName) throws InvalidFileFormatException, IOException {
        return new Track(new File(
            requireNonNull(getClass().getResource(format("/tracks/%s.txt", fileName))).getFile()));
    }

    private void simulateWin(Game game) {
        game.doCarTurn(LEFT);
        for (int i = 0; i < 45; i++) {
            game.doCarTurn(NONE);
        }
    }

    private void simulatePenalty() {
        game.doCarTurn(LEFT);
        game.doCarTurn(LEFT);
    }

    private void resetPenalty() {
        for (int i = 0; i < 4; i++) {
            game.doCarTurn(RIGHT);
        }
    }

}
